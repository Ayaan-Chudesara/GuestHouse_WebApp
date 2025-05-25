// src/app/core/models/room.model.ts

export interface Room {
  id?: number; // Optional
  roomNo: string;
  roomType: string;
  guestHouseId: number;
  guestHouseName: string; // To display guesthouse name in frontend
}