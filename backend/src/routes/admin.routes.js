const express = require('express');
const router = express.Router();
const adminController = require('../controllers/admin.controller');
const { authenticateToken, requireRole } = require('../middlewares/auth.middleware');

router.use(authenticateToken);
router.use(requireRole('ADMIN'));

router.get('/stats', adminController.getDashboardStats);
router.get('/requests', adminController.getApprovalRequests);
router.post('/requests/:id', adminController.handleApproval);

router.get('/users', adminController.getUsers);
router.post('/users', adminController.createUser);
router.delete('/users/:id', adminController.deleteUser);

router.get('/classes', adminController.getClasses);
router.post('/classes', adminController.createClass);
router.delete('/classes/:id', adminController.deleteClass);

module.exports = router;
