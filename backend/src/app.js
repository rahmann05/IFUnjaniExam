const express = require('express');
const cors = require('cors');
const compression = require('compression');
const routes = require('./routes');

const app = express();

app.use(compression()); // Compress all HTTP responses
app.use(cors());
app.use(express.json());

// Load all modular routes
app.use('/api', routes);

app.get('/', (req, res) => {
  res.status(200).json({ success: true, message: 'IF Unjani Exam API is Running!' });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ success: false, message: 'Terjadi kesalahan internal server' });
});

module.exports = app;
