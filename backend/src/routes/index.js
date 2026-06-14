const express = require('express');
const router = express.Router();

const authRoutes = require('./auth.routes');
const kelasRoutes = require('./kelas.routes');
const examRoutes = require('./exam.routes');
const adminRoutes = require('./admin.routes');

router.use('/auth', authRoutes);
router.use('/kelas', kelasRoutes);
router.use('/exams', examRoutes);
router.use('/admin', adminRoutes);

module.exports = router;
