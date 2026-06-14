const express = require('express');
const router = express.Router();
const kelasController = require('../controllers/kelas.controller');
const { authenticateToken } = require('../middlewares/auth.middleware');

router.use(authenticateToken);
router.get('/', kelasController.getMyClasses);
router.get('/:id', kelasController.getClassDetail);

module.exports = router;
