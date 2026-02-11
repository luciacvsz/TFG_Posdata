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
  console.log("✅ Conectado a la Base de Datos MySQL");
});

app.post("/api/login", (req, res) => {
  const { email, password } = req.body;

  const sql = "SELECT * FROM users WHERE email = ? AND password = ?";
  db.query(sql, [email, password], (err, results) => {
    if (err) {
      console.error(err);
      return res
        .status(500)
        .json({ success: false, message: "Error en el servidor" });
    }

    if (results.length > 0) {
      const user = results[0];

      if (user.is_active === 0) {
        return res.json({
          success: false,
          message: "Usuario inactivo. Contacta al administrador.",
        });
      }

      res.json({
        success: true,
        message: "Login correcto",
        user_id: user.user_id ? user.user_id.toString() : null,
        session_token: "token_falso_12345",
        tokens: user.tokens || 0,
      });
    } else {
      res.json({
        success: false,
        message: "Correo o contraseña incorrectos",
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
        .json({ success: false, message: "Error de base de datos" });
    }

    if (results.length > 0) {
      return res.json({
        success: true,
        message: "El usuario ya existe",
      });
    } else {
      return res.json({
        success: false,
        message: "Usuario disponible",
      });
    }
  });
});

app.post("/api/register", (req, res) => {
  const { user_id, email, password } = req.body;

  if (!user_id) {
    return res
      .status(400)
      .json({ success: false, message: "Falta el user_id de la nube" });
  }

  const sql =
    "INSERT INTO users (user_id, email, password, tokens, is_active) VALUES (?, ?, ?, 100, 1)";

  db.query(sql, [user_id, email, password], (err, result) => {
    if (err) {
      console.error("Error al insertar:", err);
      return res
        .status(500)
        .json({ success: false, message: "Error al crear usuario en local" });
    }

    res.json({
      success: true,
      message: "Registro completado con éxito",
      session_token: "token_simulado_" + Date.now(),
      tokens: 100,
    });
  });
});

const PORT = 3000;
app.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
});
