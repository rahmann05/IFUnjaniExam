const express = require('express');
const router = express.Router();
const examController = require('../controllers/exam.controller');
const { authenticateToken, requireRole } = require('../middlewares/auth.middleware');

router.use(authenticateToken); // Protect all exam routes

router.get('/', examController.getExams);
router.get('/:id/questions', examController.getExamQuestions);
router.post('/create', requireRole('DOSEN'), examController.createExam);
router.post('/:id/submit', requireRole('MAHASISWA'), examController.submitExam);

module.exports = router;
