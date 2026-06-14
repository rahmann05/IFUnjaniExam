const express = require('express');
const cors = require('cors');
const routes = require('./routes');

const app = express();

app.use(cors());
app.use(express.json());

// Load all modular routes
app.use('/api', routes);

// Halaman Utama (Root) agar tidak 404
app.get('/', (req, res) => {
  res.send(`
    <!DOCTYPE html>
    <html lang="id">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>IF Unjani Exam API</title>
      <style>
        body {
          background-color: #ffffff;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 100vh;
          margin: 0;
          font-family: Arial, sans-serif;
        }
        img {
          width: 200px;
          height: auto;
          margin-bottom: 20px;
        }
        h1 {
          color: #2c3e50;
          margin-bottom: 5px;
        }
        p {
          color: #27ae60;
          font-weight: bold;
        }
      </style>
    </head>
    <body>
      <img src="https://upload.wikimedia.org/wikipedia/id/5/5f/Logo_Unjani.png" alt="Logo Universitas Jenderal Achmad Yani" onerror="this.src='https://unjani.ac.id/wp-content/uploads/2023/04/Logo-UNJANI.png';">
      <h1>IF Unjani Exam API Server</h1>
      <p>Server Aktif & Berjalan</p>
    </body>
    </html>
  `);
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ success: false, message: 'Terjadi kesalahan internal server' });
});

module.exports = app;
