const express = require('express');
const mysql = require('mysql');
const bodyParser = require('body-parser');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(bodyParser.json());

const db = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: '',
    database: 'posdata_db'
});

db.connect((err) => {
    if (err) throw err;
    console.log('✅ Conectado a la Base de Datos MySQL');
});

app.post('/api/login', (req, res) => {
    const { email, password } = req.body;

    const sql = 'SELECT * FROM users WHERE email = ? AND password = ?';
    db.query(sql, [email, password], (err, results) => {
        if (err) {
            return res.status(500).json({ success: false, message: 'Error en el servidor' });
        }

        if (results.length > 0) {
            const user = results[0];

            if(user.is_active === 0) {
                return res.json({
                    success: false,
                    message: 'Usuario inactivo. Contacta al administrador.'
                });
            }

            res.json({
                success: true,
                message: 'Login correcto',
                userName: user.name,
                token: 'token_falso_12345'
            });
        } else {
            res.json({
                success: false,
                message: 'Correo o contraseña incorrectos'
            });
        }
    });
});

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
});