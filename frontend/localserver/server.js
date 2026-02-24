const express = require("express");
const mysql = require("mysql");
const bodyParser = require("body-parser");
const cors = require("cors");

const app = express();
app.use(cors());
app.use(bodyParser.json());

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

app.post("/api/login", (req, res) => {
  const { email, password } = req.body;

  const sql = "SELECT * FROM users WHERE email = ? AND password = ?";
  db.query(sql, [email, password], (err, results) => {
    if (err) {
      console.error(err);
      return res.status(500).json({ success: false, message: "Server error" });
    }

    if (results.length > 0) {
      const user = results[0];

      if (user.is_active === 0) {
        return res.json({
          success: false,
          message: "Inactive account. Please contact support.",
        });
      }

      res.json({
        success: true,
        message: "Correct login",
        user_id: user.user_id ? user.user_id.toString() : null,
        tokens: user.tokens || 0,
      });
    } else {
      res.json({
        success: false,
        message: "Email or password incorrect",
      });
    }
  });
});

app.post("/api/check-email", (req, res) => {
  const { email } = req.body;

  const sql = "SELECT email FROM users WHERE email = ?";
  db.query(sql, [email], (err, results) => {
    if (err) {
      console.error(err);
      return res
        .status(500)
        .json({ success: false, message: "Database error" });
    }

    if (results.length > 0) {
      return res.json({
        success: true,
        message: "The user already exists",
      });
    } else {
      return res.json({
        success: false,
        message: "The user does not exist",
      });
    }
  });
});

app.post("/api/register", (req, res) => {
  const { user_id } = req.query;
  const { email, password } = req.body;

  if (!user_id) {
    return res
      .status(400)
      .json({ success: false, message: "Missing user_id from cloud" });
  }

  const sql =
    "INSERT INTO users (user_id, email, password, tokens, is_active) VALUES (?, ?, ?, 100, 1)";

  db.query(sql, [user_id, email, password], (err, result) => {
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
      tokens: 100,
    });
  });
});

app.delete("/api/user", (req, res) => {
  const { user_id } = req.query;

  if (!user_id) {
    return res
      .status(400)
      .json({ success: false, message: "Missing user_id from cloud" });
  }

  const sql = "DELETE FROM users WHERE user_id = ?";

  db.query(sql, [user_id], (err, result) => {
    if (err) {
      console.error("Error while deleting user:", err);
      return res.status(500).json({
        success: false,
        message: "Error deleting user in local database",
      });
    }

    res.json({
      success: true,
      message: "User deleted successfully",
    });
  });
});

app.patch("/api/user", (req, res) => {
  const { user_id } = req.query;
  const { email, password } = req.body;

  if (!user_id) {
    return res
      .status(400)
      .json({ success: false, message: "Missing user_id from cloud" });
  }

  if (email != undefined) {
    const sqlEmail = "UPDATE users SET email = ? WHERE user_id = ?";

    db.query(sqlEmail, [email, user_id], (err, result) => {
      if (err) {
        console.error("Error updating email:", err);
        return res.status(500).json({
          success: false,
          message: "Error updating email in local database",
        });
      }

      return res.json({
        success: true,
        message: "Email updated successfully",
      });
    });
  }

  if (password != undefined) {
    const sqlPassword = "UPDATE users SET password = ? WHERE user_id = ?";

    db.query(sqlPassword, [password, user_id], (err, result) => {
      if (err) {
        console.error("Error updating password:", err);
        return res.status(500).json({
          success: false,
          message: "Error updating password in local database",
        });
      }

      return res.json({
        success: true,
        message: "Password updated successfully",
      });
    });
  }
});

const PORT = 3000;
app.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT}`);
});
