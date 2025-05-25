// src/app/core/models/user.model.ts

export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN'
}

export interface User {
  id?: number;
  name: string;
  email: string;
  phone: string;
  role: UserRole;
  enabled: boolean; // Assuming you have an enabled field for user accounts
}