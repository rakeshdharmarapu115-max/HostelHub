import { prisma } from '../../config/prisma';
import { HostelGenderType } from '../../types/enums';

export class HostelsService {
  async getHostels(filters?: {
    city?: string;
    gender?: string;
    minRent?: number;
    maxRent?: number;
  }) {
    const where: any = {};

    if (filters?.city) {
      where.city = { contains: filters.city, mode: 'insensitive' };
    }

    if (filters?.gender) {
      where.genderType = filters.gender.toUpperCase() as HostelGenderType;
    }

    if (filters?.minRent !== undefined || filters?.maxRent !== undefined) {
      where.baseMonthlyRent = {};
      if (filters?.minRent !== undefined) where.baseMonthlyRent.gte = Number(filters.minRent);
      if (filters?.maxRent !== undefined) where.baseMonthlyRent.lte = Number(filters.maxRent);
    }

    const hostels = await prisma.hostel.findMany({
      where,
      include: {
        host: { select: { fullName: true, businessName: true, contactPhone: true, contactEmail: true } },
        reviews: { orderBy: { createdAt: 'desc' }, take: 5 }
      },
      orderBy: { rating: 'desc' }
    });

    return hostels.map(h => this.mapHostel(h));
  }

  async getHostelById(id: string) {
    const hostel = await prisma.hostel.findUnique({
      where: { id },
      include: {
        host: true,
        reviews: {
          orderBy: { createdAt: 'desc' }
        },
        blocks: {
          include: {
            floors: {
              include: {
                rooms: {
                  include: { beds: true }
                }
              }
            }
          }
        },
        rooms: {
          include: { beds: true }
        }
      }
    });

    if (!hostel) {
      throw { status: 404, message: `Hostel not found for ID: ${id}` };
    }

    return this.mapHostel(hostel);
  }

  async createHostel(data: {
    hostId: string;
    name: string;
    address: string;
    city: string;
    state?: string;
    postalCode?: string;
    latitude?: number;
    longitude?: number;
    description?: string;
    genderType?: HostelGenderType;
    amenities?: string[];
    rules?: string[];
    images?: string[];
    totalRooms?: number;
    totalBeds?: number;
    baseMonthlyRent?: number;
    cautionDeposit?: number;
    contactEmail?: string;
    contactPhone?: string;
  }) {
    const created = await prisma.hostel.create({
      data: {
        hostId: data.hostId,
        name: data.name,
        address: data.address,
        city: data.city,
        state: data.state || '',
        postalCode: data.postalCode || '',
        latitude: data.latitude || 0.0,
        longitude: data.longitude || 0.0,
        description: data.description,
        genderType: data.genderType || HostelGenderType.COED,
        amenities: Array.isArray(data.amenities) ? JSON.stringify(data.amenities) : (data.amenities || '[]'),
        rules: Array.isArray(data.rules) ? JSON.stringify(data.rules) : (data.rules || '[]'),
        images: Array.isArray(data.images) ? JSON.stringify(data.images) : (data.images || '[]'),
        totalRooms: data.totalRooms || 0,
        totalBeds: data.totalBeds || 0,
        baseMonthlyRent: data.baseMonthlyRent || 0.0,
        cautionDeposit: data.cautionDeposit || 0.0,
        contactEmail: data.contactEmail,
        contactPhone: data.contactPhone
      }
    });

    return this.mapHostel(created);
  }

  async updateHostel(id: string, data: Partial<any>) {
    const updatePayload: any = { ...data };
    if (data.amenities !== undefined) updatePayload.amenities = Array.isArray(data.amenities) ? JSON.stringify(data.amenities) : data.amenities;
    if (data.rules !== undefined) updatePayload.rules = Array.isArray(data.rules) ? JSON.stringify(data.rules) : data.rules;
    if (data.images !== undefined) updatePayload.images = Array.isArray(data.images) ? JSON.stringify(data.images) : data.images;

    const updated = await prisma.hostel.update({
      where: { id },
      data: updatePayload
    });

    return this.mapHostel(updated);
  }

  /**
   * Search nearby hostels based on location (latitude, longitude, radius).
   * Calculates Haversine distance, filters, sorts by nearest first.
   */
  async searchNearbyHostels(params: {
    lat?: number;
    lng?: number;
    radius?: number; // in kilometers, e.g. 1, 5, 10, 25, 50
    city?: string;
    gender?: string;
    query?: string;
    minRent?: number;
    maxRent?: number;
  }) {
    const where: any = {};

    if (params.city) {
      where.city = { contains: params.city, mode: 'insensitive' };
    }

    if (params.gender) {
      where.genderType = params.gender.toUpperCase() as HostelGenderType;
    }

    if (params.minRent !== undefined || params.maxRent !== undefined) {
      where.baseMonthlyRent = {};
      if (params.minRent !== undefined) where.baseMonthlyRent.gte = Number(params.minRent);
      if (params.maxRent !== undefined) where.baseMonthlyRent.lte = Number(params.maxRent);
    }

    if (params.query) {
      where.OR = [
        { name: { contains: params.query, mode: 'insensitive' } },
        { address: { contains: params.query, mode: 'insensitive' } },
        { city: { contains: params.query, mode: 'insensitive' } }
      ];
    }

    const hostels = await prisma.hostel.findMany({
      where,
      include: {
        host: { select: { fullName: true, businessName: true, contactPhone: true, contactEmail: true } },
        reviews: { orderBy: { createdAt: 'desc' }, take: 5 },
        rooms: { select: { id: true, roomNumber: true, totalCapacity: true, occupiedCount: true, status: true, monthlyRent: true } }
      }
    });

    const userLat = params.lat !== undefined && !isNaN(Number(params.lat)) ? Number(params.lat) : undefined;
    const userLng = params.lng !== undefined && !isNaN(Number(params.lng)) ? Number(params.lng) : undefined;
    const radiusKm = params.radius !== undefined && !isNaN(Number(params.radius)) ? Number(params.radius) : undefined;

    const mapped = hostels.map(h => {
      const baseHostel = this.mapHostel(h);
      let distanceKm: number | null = null;

      if (userLat !== undefined && userLng !== undefined && h.latitude && h.longitude && (h.latitude !== 0 || h.longitude !== 0)) {
        distanceKm = this.calculateHaversineDistance(userLat, userLng, h.latitude, h.longitude);
      }

      // Calculate total available beds
      const totalRooms = h.rooms.length;
      const totalCapacity = h.rooms.reduce((sum, r) => sum + r.totalCapacity, 0);
      const totalOccupied = h.rooms.reduce((sum, r) => sum + r.occupiedCount, 0);
      const availableBeds = Math.max(0, (h.totalBeds || totalCapacity) - (h.occupiedBeds || totalOccupied));

      return {
        ...baseHostel,
        distanceKm,
        availableBeds,
        availableRoomsCount: h.rooms.filter(r => r.occupiedCount < r.totalCapacity).length
      };
    });

    // If radius is specified and user coordinates provided, filter by radius
    let filtered = mapped;
    if (userLat !== undefined && userLng !== undefined && radiusKm !== undefined && radiusKm > 0) {
      filtered = mapped.filter(h => h.distanceKm !== null && h.distanceKm <= radiusKm);
    }

    // Sort by distance if available, otherwise rating
    if (userLat !== undefined && userLng !== undefined) {
      filtered.sort((a, b) => {
        if (a.distanceKm !== null && b.distanceKm !== null) {
          return a.distanceKm - b.distanceKm;
        }
        if (a.distanceKm !== null) return -1;
        if (b.distanceKm !== null) return 1;
        return b.rating - a.rating;
      });
    } else {
      filtered.sort((a, b) => b.rating - a.rating);
    }

    return filtered;
  }

  /**
   * Update hostel physical location coordinates and address.
   */
  async updateHostelLocation(hostelId: string, data: {
    latitude: number;
    longitude: number;
    address?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    requesterHostId?: string;
    requesterRole?: string;
  }) {
    if (typeof data.latitude !== 'number' || data.latitude < -90 || data.latitude > 90) {
      throw { status: 400, message: 'Invalid latitude. Must be between -90 and 90.' };
    }
    if (typeof data.longitude !== 'number' || data.longitude < -180 || data.longitude > 180) {
      throw { status: 400, message: 'Invalid longitude. Must be between -180 and 180.' };
    }

    const hostel = await prisma.hostel.findUnique({ where: { id: hostelId } });
    if (!hostel) {
      throw { status: 404, message: `Hostel not found for ID: ${hostelId}` };
    }

    if (data.requesterRole === 'HOST' && data.requesterHostId && hostel.hostId !== data.requesterHostId) {
      throw { status: 403, message: 'You are not authorized to update location for this hostel.' };
    }

    const updated = await prisma.hostel.update({
      where: { id: hostelId },
      data: {
        latitude: data.latitude,
        longitude: data.longitude,
        ...(data.address ? { address: data.address.trim() } : {}),
        ...(data.city ? { city: data.city.trim() } : {}),
        ...(data.state ? { state: data.state.trim() } : {}),
        ...(data.postalCode ? { postalCode: data.postalCode.trim() } : {})
      }
    });

    return this.mapHostel(updated);
  }

  private calculateHaversineDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371; // Earth's radius in km
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return Math.round(R * c * 10) / 10;
  }

  async addHostelImages(id: string, newImages: string[]) {
    const hostel = await prisma.hostel.findUnique({ where: { id } });
    if (!hostel) {
      throw { status: 404, message: `Hostel not found for ID: ${id}` };
    }

    let existingImages: string[] = [];
    try {
      existingImages = JSON.parse(hostel.images || '[]');
    } catch {
      existingImages = [];
    }

    const merged = Array.from(new Set([...existingImages, ...newImages]));

    const updated = await prisma.hostel.update({
      where: { id },
      data: { images: JSON.stringify(merged) }
    });

    return this.mapHostel(updated);
  }

  async addReview(data: {
    hostelId: string;
    studentId: string;
    studentName?: string;
    rating: number;
    comment?: string;
    cleanliness?: number;
    foodQuality?: number;
    amenitiesRating?: number;
  }) {
    let student = await prisma.student.findFirst({
      where: {
        OR: [
          { id: data.studentId },
          { userId: data.studentId },
          { rollNumber: data.studentId }
        ]
      }
    });

    const studentId = student?.id || data.studentId;
    const studentName = student?.fullName || data.studentName || 'Resident Student';

    return prisma.$transaction(async (tx) => {
      const review = await tx.hostelReview.create({
        data: {
          hostelId: data.hostelId,
          studentId,
          studentName,
          rating: Number(data.rating) || 5.0,
          comment: data.comment || 'Great hostel with clean rooms and supportive management.',
          cleanliness: Number(data.cleanliness) || 5.0,
          foodQuality: Number(data.foodQuality) || 5.0,
          amenitiesRating: Number(data.amenitiesRating) || 5.0
        }
      });

      // Recalculate average rating
      const allReviews = await tx.hostelReview.findMany({
        where: { hostelId: data.hostelId }
      });

      const avgRating = allReviews.reduce((sum, r) => sum + r.rating, 0) / allReviews.length;

      await tx.hostel.update({
        where: { id: data.hostelId },
        data: {
          rating: Math.round(avgRating * 10) / 10,
          ratingCount: allReviews.length
        }
      });

      return {
        reviewId: review.id,
        hostelId: review.hostelId,
        studentId: review.studentId,
        studentName: review.studentName,
        rating: review.rating,
        comment: review.comment,
        cleanliness: review.cleanliness,
        foodQuality: review.foodQuality,
        amenitiesRating: review.amenitiesRating,
        createdAt: review.createdAt.getTime(),
        updatedHostelRating: Math.round(avgRating * 10) / 10,
        totalReviews: allReviews.length
      };
    });
  }

  async getReviews(hostelId: string) {
    const reviews = await prisma.hostelReview.findMany({
      where: { hostelId },
      orderBy: { createdAt: 'desc' }
    });

    return reviews.map(r => ({
      reviewId: r.id,
      hostelId: r.hostelId,
      studentId: r.studentId,
      studentName: r.studentName,
      rating: r.rating,
      comment: r.comment,
      cleanliness: r.cleanliness,
      foodQuality: r.foodQuality,
      amenitiesRating: r.amenitiesRating,
      createdAt: r.createdAt.getTime()
    }));
  }

  async deleteHostel(id: string) {
    await prisma.hostel.delete({
      where: { id }
    });

    return { success: true };
  }

  private mapHostel(h: any) {
    const parseJsonArray = (val: any) => {
      if (Array.isArray(val)) return val;
      if (typeof val === 'string') {
        try { return JSON.parse(val); } catch { return []; }
      }
      return [];
    };

    return {
      hostelId: h.id,
      hostId: h.hostId,
      name: h.name,
      address: h.address,
      city: h.city,
      state: h.state || '',
      postalCode: h.postalCode || '',
      latitude: h.latitude || 0.0,
      longitude: h.longitude || 0.0,
      description: h.description || '',
      genderType: h.genderType,
      amenities: parseJsonArray(h.amenities),
      rules: parseJsonArray(h.rules),
      images: parseJsonArray(h.images),
      totalRooms: h.totalRooms || 0,
      totalBeds: h.totalBeds || 0,
      occupiedBeds: h.occupiedBeds || 0,
      baseMonthlyRent: h.baseMonthlyRent || 0.0,
      cautionDeposit: h.cautionDeposit || 0.0,
      rating: h.rating || 0.0,
      ratingCount: h.ratingCount || 0,
      contactEmail: h.contactEmail || '',
      contactPhone: h.contactPhone || '',
      createdAt: h.createdAt.getTime(),
      reviews: h.reviews?.map((r: any) => ({
        reviewId: r.id,
        studentName: r.studentName,
        rating: r.rating,
        comment: r.comment,
        createdAt: r.createdAt.getTime()
      })) || [],
      blocks: h.blocks,
      rooms: h.rooms
    };
  }
}
