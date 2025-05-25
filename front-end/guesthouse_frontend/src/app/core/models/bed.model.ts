// src/app/core/models/bed.model.ts

export enum BedStatus {
  AVAILABLE = 'AVAILABLE',
  BOOKED = 'BOOKED',
  UNDER_MAINTENANCE = 'UNDER_MAINTENANCE'
}

export interface Bed {
  id?: number; // Optional
  bedNo: string;
  status: BedStatus; // Directly corresponds to Bed.Status enum
  roomId: number; // Foreign key
  roomNo: string; // To display room number in frontend
}