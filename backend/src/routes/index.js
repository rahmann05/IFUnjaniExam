const express = require('express');
const router = express.Router();

const authRoutes = require('./auth.routes');
const examRoutes = require('./exam.routes');

router.use('/auth', authRoutes);
router.use('/exams', examRoutes);

module.exports = router;
