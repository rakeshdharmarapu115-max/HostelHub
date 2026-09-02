import { Request, Response, NextFunction } from 'express';
import { HostelsService } from './hostels.service';
import { sendSuccess, sendError } from '../../utils/apiResponse';
import { AuthenticatedRequest } from '../../middleware/auth.middleware';
import { UserRole } from '../../types/enums';

const hostelsService = new HostelsService();

export class HostelsController {
  async getHostels(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { city, gender, minRent, maxRent } = req.query;
      const hostels = await hostelsService.getHostels({
        city: city as string,
        gender: gender as string,
        minRent: minRent ? Number(minRent) : undefined,
        maxRent: maxRent ? Number(maxRent) : undefined
      });
      sendSuccess(res, 'Hostels retrieved successfully', hostels);
    } catch (error) {
      next(error);
    }
  }

  async getHostelById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const hostel = await hostelsService.getHostelById(req.params.id);
      sendSuccess(res, 'Hostel details retrieved', hostel);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async createHostel(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      let hostId = req.body.hostId;
      if (req.user?.role === UserRole.HOST) {
        hostId = req.user.profileId;
      }
      if (!hostId) {
        sendError(res, 'Host ID is required to create a hostel', 400);
        return;
      }
      const created = await hostelsService.createHostel({ ...req.body, hostId });
      sendSuccess(res, 'Hostel created successfully', created, 201);
    } catch (error) {
      next(error);
    }
  }

  async updateHostel(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const updated = await hostelsService.updateHostel(req.params.id, req.body);
      sendSuccess(res, 'Hostel updated successfully', updated);
    } catch (error) {
      next(error);
    }
  }

  async addHostelImages(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const images = Array.isArray(req.body.images) ? req.body.images : [req.body.imageUrl || req.body.image];
      const updated = await hostelsService.addHostelImages(req.params.id, images.filter(Boolean));
      sendSuccess(res, 'Hostel images uploaded and added successfully', updated);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async addReview(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const studentId = req.body.studentId || req.user?.profileId || req.user?.userId;
      const studentName = req.body.studentName || req.user?.fullName || 'Student';
      if (!req.body.rating) {
        sendError(res, 'Rating (1-5 stars) is required', 400);
        return;
      }

      const review = await hostelsService.addReview({
        hostelId: req.params.id,
        studentId,
        studentName,
        rating: Number(req.body.rating),
        comment: req.body.comment,
        cleanliness: req.body.cleanliness ? Number(req.body.cleanliness) : undefined,
        foodQuality: req.body.foodQuality ? Number(req.body.foodQuality) : undefined,
        amenitiesRating: req.body.amenitiesRating ? Number(req.body.amenitiesRating) : undefined
      });
      sendSuccess(res, 'Hostel rating & review submitted successfully', review, 201);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async getReviews(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const reviews = await hostelsService.getReviews(req.params.id);
      sendSuccess(res, 'Hostel reviews retrieved', reviews);
    } catch (error) {
      next(error);
    }
  }

  async searchNearby(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { lat, lng, radius, city, gender, query, minRent, maxRent } = req.query;
      const hostels = await hostelsService.searchNearbyHostels({
        lat: lat ? Number(lat) : undefined,
        lng: lng ? Number(lng) : undefined,
        radius: radius ? Number(radius) : undefined,
        city: city as string,
        gender: gender as string,
        query: query as string,
        minRent: minRent ? Number(minRent) : undefined,
        maxRent: maxRent ? Number(maxRent) : undefined
      });
      sendSuccess(res, 'Hostels found successfully', hostels);
    } catch (error) {
      next(error);
    }
  }

  async updateLocation(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const hostelId = req.params.id;
      const { latitude, longitude, address, city, state, postalCode } = req.body;
      const requesterHostId = req.user?.profileId;
      const requesterRole = req.user?.role;

      const updated = await hostelsService.updateHostelLocation(hostelId, {
        latitude: Number(latitude),
        longitude: Number(longitude),
        address,
        city,
        state,
        postalCode,
        requesterHostId,
        requesterRole
      });

      sendSuccess(res, 'Hostel location updated successfully', updated);
    } catch (error: any) {
      if (error.status) {
        sendError(res, error.message, error.status);
      } else {
        next(error);
      }
    }
  }

  async deleteHostel(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const result = await hostelsService.deleteHostel(req.params.id);
      sendSuccess(res, 'Hostel deleted successfully', result);
    } catch (error) {
      next(error);
    }
  }
}
