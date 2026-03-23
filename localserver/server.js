const express = require("express");
const mysql = require("mysql");
const cors = require("cors");
const app = express();
const PORT = 3000;
const INITIAL_TOKENS = 100;
const bcrypt = require("bcrypt");
const saltRounds = 10;

app.use(cors());
app.use(express.json());

app.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT}`);
});

const USER_DEACTIVATE_QUERY =
  "UPDATE users SET is_active = 0 WHERE user_id = ?";
const USER_GET_QUERY = "SELECT email FROM users WHERE email = ?";
const USER_UPDATE_EMAIL_QUERY = "UPDATE users SET email = ? WHERE user_id = ?";
const USER_PASSWORD_UPDATE_QUERY =
  "UPDATE users SET password = ? WHERE user_id = ?";
const USER_TOKENS_GET_QUERY = "SELECT tokens FROM users WHERE user_id = ?";
const USER_TOKENS_DEDUCT_QUERY =
  "UPDATE users SET tokens = tokens - ? WHERE user_id = ? AND tokens >= ?";
const LOGIN_POST_QUERY = "SELECT * FROM users WHERE email = ?";
const USER_PUT_QUERY = `INSERT INTO users (user_id, email, password, tokens, is_active) VALUES (?, ?, ?, ?, 1)`;

const db = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: "",
  database: "posdata_db",
});

db.connect((err) => {
  if (err) throw err;
  console.log("Connected to MySQL Database");
});

app.delete("/users/:user_id", (req, res) => {
  const { user_id } = req.params;

  db.query(USER_DEACTIVATE_QUERY, [user_id], (err, result) => {
    if (err) {
      console.error("Error while deactivating user:", err);
      return res.status(500).json({
        success: false,
        message: "Error deactivating user in local database",
      });
    }

    if (result.affectedRows === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    res.json({
      success: true,
      message: "User deactivated successfully",
    });
  });
});

app.get("/users/:email", (req, res) => {
  const { email } = req.params;

  db.query(USER_GET_QUERY, [email], (err, results) => {
    if (err) {
      console.error("Error while checking user existence:", err);
      return res.status(500).json({
        success: false,
        message: "Error checking user existence in local database",
      });
    }

    if (results.length > 0) {
      return res.json({
        success: true,
        message: "The user already exists",
      });
    }

    return res.json({
      success: false,
      message: "The user does not exist",
    });
  });
});

app.patch("/users/:user_id", async (req, res) => {
  const { user_id } = req.params;
  const { email, password } = req.body;

  try {
    if (email) {
      db.query(USER_UPDATE_EMAIL_QUERY, [email, user_id], (err, result) => {
        if (err) {
          console.error("Error updating email:", err);
          return res.status(500).json({
            success: false,
            message: "Error updating email in local database",
          });
        }

        if (result.affectedRows != 1) {
          return res.status(404).json({
            success: false,
            message: "User not found",
          });
        }

        return res.json({
          success: true,
          message: "Email updated successfully",
        });
      });
    }

    if (password) {
      const secureHash = await bcrypt.hash(password, saltRounds);

      db.query(
        USER_PASSWORD_UPDATE_QUERY,
        [secureHash, user_id],
        (err, result) => {
          if (err) {
            console.error("Error updating password:", err);
            return res.status(500).json({
              success: false,
              message: "Error updating password in local database",
            });
          }

          if (result.affectedRows != 1) {
            return res.status(404).json({
              success: false,
              message: "User not found",
            });
          }

          return res.json({
            success: true,
            message: "Password updated successfully",
          });
        },
      );
    }
  } catch (err) {
    console.error("Error processing password:", err);
    return res.status(500).json({
      success: false,
      message: "Error processing password",
    });
  }
});

const CLOUD_COSTS = {
  POST_SMS: 3,
  GET_USER: 10,
  PATCH_USER: 8,
  DELETE_USER: 1,
};

app.patch("/users/:user_id/tokens", (req, res) => {
  const { user_id } = req.params;
  const { operation } = req.body;

  if (!operation || !CLOUD_COSTS[operation]) {
    return res.status(400).json({
      success: false,
      message: "Invalid operation",
    });
  }

  const cost = CLOUD_COSTS[operation];

  db.query(USER_TOKENS_GET_QUERY, [user_id], (err, results) => {
    if (err) {
      console.error("Error consulting tokens:", err);
      return res.status(500).json({
        success: false,
        message: "Error consulting token balance",
      });
    }

    if (results.length === 0) {
      return res.status(404).json({
        success: false,
        message: "User not found",
      });
    }

    const currentTokens = results[0].tokens;

    if (currentTokens < cost) {
      return res.status(400).json({
        success: false,
        message:
          "Insufficient token balance. Please contact the service administrator.",
      });
    }

    db.query(USER_TOKENS_DEDUCT_QUERY, [cost, user_id, cost], (err, result) => {
      if (err) {
        console.error("Error consulting tokens:", err);
        return res.status(500).json({
          success: false,
          message: "Error consulting token balance",
        });
      }

      if (result.affectedRows === 0) {
        return res.status(400).json({
          success: false,
          message:
            "Insufficient token balance. Please contact the service administrator.",
        });
      }

      res.json({
        success: true,
        message: "Tokens deducted successfully",
      });
    });
  });
});

app.post("/login", (req, res) => {
  const { email, password } = req.body;

  db.query(LOGIN_POST_QUERY, [email], async (err, results) => {
    if (err) {
      console.error(err);
      return res.status(500).json({
        success: false,
        message: "Error during login process",
      });
    }

    if (results.length != 1) {
      return res.json({
        success: false,
        message: "Email or password incorrect",
      });
    }

    if (results[0].is_active === 0) {
      return res.json({
        success: false,
        message: "Inactive account. Please contact support.",
      });
    }

    const match = await bcrypt.compare(password, results[0].password);
    if (!match) {
      return res.json({
        success: false,
        message: "Email or password incorrect",
      });
    }

    res.json({
      success: true,
      message: "Correct login",
      user_id: results[0].user_id,
    });
  });
});

app.put("/users/:user_id", async (req, res) => {
  const { user_id } = req.params;
  const { email, password } = req.body;

  try {
    const secureHash = await bcrypt.hash(password, saltRounds);

    db.query(
      USER_PUT_QUERY,
      [user_id, email, secureHash, INITIAL_TOKENS],
      (err, result) => {
        if (err) {
          console.error("Error while inserting user:", err);
          return res.status(500).json({
            success: false,
            message: "Error creating user in local database",
          });
        }

        res.json({
          success: true,
          message: "Registration successful",
        });
      },
    );
  } catch (err) {
    console.error("Error hashing password:", err);
    return res.status(500).json({
      success: false,
      message: "Error processing password",
    });
  }
});
