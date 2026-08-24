import { prisma } from '../../config/prisma';

const DEFAULT_SCHEDULE = {
  monday: {
    breakfast: ['Poha', 'Boiled Eggs / Sprouts', 'Masala Chai', 'Filter Coffee'],
    lunch: ['Steamed Basmati Rice', 'Dal Tadka', 'Paneer Butter Masala', 'Curd', 'Salad'],
    snacks: ['Vegetable Samosa', 'Masala Chai', 'Biscuits'],
    dinner: ['Butter Roti', 'Mixed Veg Curry', 'Jeera Rice', 'Gulab Jamun']
  },
  tuesday: {
    breakfast: ['Masala Dosa', 'Coconut Chutney', 'Sambar', 'Tea / Coffee'],
    lunch: ['Jeera Rice', 'Rajma Masala', 'Aloo Gobi', 'Raita', 'Papad'],
    snacks: ['Pani Puri / Bhel Puri', 'Ginger Tea'],
    dinner: ['Chapati', 'Paneer Kadhai', 'Dal Fry', 'Steamed Rice', 'Kheer']
  },
  wednesday: {
    breakfast: ['Idli Vada Combo', 'Sambar', 'Tomato Chutney', 'Tea / Coffee'],
    lunch: ['Veg Biryani', 'Mirchi Ka Salan', 'Boondi Raita', 'Green Salad'],
    snacks: ['Aloo Tikki', 'Mint Chutney', 'Tea'],
    dinner: ['Phulka Roti', 'Chole Masala', 'Steamed Rice', 'Ice Cream']
  },
  thursday: {
    breakfast: ['Aloo Paratha', 'Butter', 'Curd', 'Pickle', 'Masala Chai'],
    lunch: ['Lemon Rice', 'Dal Makhani', 'Bhindi Fry', 'Curd', 'Papad'],
    snacks: ['Veg Puff', 'Coffee / Tea'],
    dinner: ['Tandoori Roti', 'Veg Korma', 'Peas Pulao', 'Fruit Custard']
  },
  friday: {
    breakfast: ['Uttapam', 'Sambar', 'Coconut Chutney', 'Filter Coffee'],
    lunch: ['South Indian Thali', 'Sambar', 'Rasam', 'Poriyal', 'Appalam', 'Curd Rice'],
    snacks: ['Onion Pakora', 'Masala Chai'],
    dinner: ['Naan', 'Paneer Tikka Masala', 'Veg Pulao', 'Rasgulla']
  },
  saturday: {
    breakfast: ['Poori Bhaji', 'Halwa', 'Masala Chai'],
    lunch: ['Fried Rice', 'Veg Manchurian', 'Sweet Corn Soup', 'Kimchi Salad'],
    snacks: ['Bread Roll', 'Green Chutney', 'Tea'],
    dinner: ['Rumali Roti', 'Dal Tadka', 'Dum Aloo', 'Jeera Rice', 'Jalebi']
  },
  sunday: {
    breakfast: ['Chole Bhature', 'Lassi', 'Masala Chai'],
    lunch: ['Special Sunday Feast: Paneer Dum Biryani', 'Raita', 'Mirchi Ka Salan', 'Double Ka Meetha'],
    snacks: ['Samosa Chaat', 'Cardamom Tea'],
    dinner: ['Butter Naan', 'Shahi Paneer', 'Dal Makhani', 'Pulao', 'Ice Cream']
  }
};

export class FoodMenuService {
  async getWeeklyMenu(hostelId?: string, weekStartDate?: string) {
    let targetHostelId = hostelId;
    if (!targetHostelId) {
      const firstHostel = await prisma.hostel.findFirst();
      targetHostelId = firstHostel?.id || 'hostel_001';
    }

    let menu;

    if (weekStartDate) {
      menu = await prisma.foodMenu.findUnique({
        where: {
          hostelId_weekStartDate: {
            hostelId: targetHostelId,
            weekStartDate
          }
        }
      });
    }

    if (!menu) {
      menu = await prisma.foodMenu.findFirst({
        where: { hostelId: targetHostelId },
        orderBy: { updatedAt: 'desc' }
      });
    }

    if (!menu) {
      // If no menu in DB, create default menu for this hostel
      menu = await prisma.foodMenu.create({
        data: {
          hostelId: targetHostelId,
          weekStartDate: weekStartDate || '2026-10-19',
          scheduleJson: JSON.stringify(DEFAULT_SCHEDULE),
          specialNotice: 'Sunday Special Feast: Served between 12:30 PM and 3:00 PM.',
          isPublished: true
        }
      });
    }

    return this.mapFoodMenu(menu);
  }

  async createOrUpdateMenu(data: {
    hostelId?: string;
    weekStartDate?: string;
    schedule: any;
    specialNotice?: string;
    isPublished?: boolean;
  }) {
    let targetHostelId = data.hostelId;
    if (!targetHostelId || targetHostelId.trim() === '') {
      const firstHostel = await prisma.hostel.findFirst();
      targetHostelId = firstHostel?.id || 'hostel_001';
    }

    const weekStartDate = data.weekStartDate || '2026-10-19';
    const scheduleJson = typeof data.schedule === 'string' ? data.schedule : JSON.stringify(data.schedule);

    const menu = await prisma.foodMenu.upsert({
      where: {
        hostelId_weekStartDate: {
          hostelId: targetHostelId,
          weekStartDate
        }
      },
      update: {
        scheduleJson,
        specialNotice: data.specialNotice,
        isPublished: data.isPublished !== undefined ? data.isPublished : true
      },
      create: {
        hostelId: targetHostelId,
        weekStartDate,
        scheduleJson,
        specialNotice: data.specialNotice,
        isPublished: data.isPublished !== undefined ? data.isPublished : true
      }
    });

    return this.mapFoodMenu(menu);
  }

  async deleteMenu(id: string) {
    await prisma.foodMenu.deleteMany({
      where: { id }
    });

    return { success: true };
  }

  private mapFoodMenu(m: any) {
    let schedule = {};
    try {
      schedule = JSON.parse(m.scheduleJson);
    } catch {
      schedule = DEFAULT_SCHEDULE;
    }

    return {
      menuId: m.id,
      hostelId: m.hostelId,
      weekStartDate: m.weekStartDate,
      schedule,
      specialNotice: m.specialNotice,
      isPublished: m.isPublished,
      updatedAt: m.updatedAt ? m.updatedAt.getTime() : Date.now()
    };
  }
}
