const express = require('express');
const router = express.Router();
const adminController = require('../controllers/admin.controller');
const { authenticateToken, requireRole } = require('../middlewares/auth.middleware');

router.use(authenticateToken);
router.use(requireRole('ADMIN'));

router.get('/stats', adminController.getDashboardStats);
router.get('/requests', adminController.getApprovalRequests);
router.post('/requests/:id', adminController.handleApproval);

module.exports = router;
