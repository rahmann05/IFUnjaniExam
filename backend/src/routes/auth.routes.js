const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');

router.post('/login', authController.login);
router.put('/change-password', authenticateToken, authController.changePassword);

module.exports = router;
