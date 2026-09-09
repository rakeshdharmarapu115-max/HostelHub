import { prisma } from '../config/prisma';

async function checkRooms() {
  console.log('=== CHECKING ALL ROOMS IN POSTGRESQL DATABASE ===');
  const hostels = await prisma.hostel.findMany();
  console.log(`Found ${hostels.length} hostels:`);
  for (const h of hostels) {
    console.log(`- Hostel: ${h.name} (ID: ${h.id})`);
  }

  const rooms = await prisma.room.findMany({
    include: { beds: true, hostel: true }
  });
  console.log(`\nFound ${rooms.length} rooms in DB:`);
  for (const r of rooms) {
    console.log(`- Room: "${r.roomNumber}" | ID: "${r.id}" | Hostel: "${r.hostel?.name}" (${r.hostelId}) | Capacity: ${r.totalCapacity} | Occupied: ${r.occupiedCount} | Status: ${r.status}`);
    for (const b of r.beds) {
      console.log(`    Bed: "${b.bedNumber}" (ID: "${b.id}", occupied: ${b.isOccupied})`);
    }
  }
}

checkRooms().finally(() => prisma.$disconnect());
