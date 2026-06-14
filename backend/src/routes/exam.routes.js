const express = require('express');
const router = express.Router();
const examController = require('../controllers/exam.controller');
const { authenticateToken, requireRole } = require('../middlewares/auth.middleware');

router.use(authenticateToken); // Protect all exam routes

router.get('/', examController.getExams);
router.get('/:id/questions', examController.getExamQuestions);
router.post('/create', requireRole('DOSEN'), examController.createExam);
router.post('/:id/submit', requireRole('MAHASISWA'), examController.submitExam);

router.get('/:id/results', requireRole('DOSEN'), examController.getExamResults);
router.get('/attempts/:id', requireRole('DOSEN'), examController.getAttemptDetail);
router.delete('/:id', requireRole('DOSEN'), examController.deleteExam);
router.post('/:id/request-approval', requireRole('DOSEN'), examController.requestApproval);

module.exports = router;
