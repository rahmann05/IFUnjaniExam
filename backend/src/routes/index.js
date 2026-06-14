const express = require('express');
const router = express.Router();

const authRoutes = require('./auth.routes');
const examRoutes = require('./exam.routes');
const kelasRoutes = require('./kelas.routes');

router.use('/auth', authRoutes);
router.use('/exams', examRoutes);
router.use('/kelas', kelasRoutes);

module.exports = router;
