import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core'; // <--- Added OnInit
import { forkJoin } from 'rxjs';
import { Bed, BedStatus } from 'src/app/core/models/bed.model';
import { GuestHouse } from 'src/app/core/models/guesthouse.model';
import { Room } from 'src/app/core/models/room.model';
import { BedService } from '../services/bed.service';
import { RoomService } from '../services/room.service';
import { GuesthouseService } from '../services/guesthouse.service';

@Component({
  selector: 'app-admin-manage',
  templateUrl: './admin-manage.component.html',
  styleUrls: ['./admin-manage.component.css']
})
export class AdminManageComponent implements OnInit { // <--- Implemented OnInit

  // Data lists
  guesthouses: GuestHouse[] = [];
  rooms: Room[] = [];
  beds: Bed[] = [];

  // Form related properties
  selectedGuestHouse: GuestHouse | null = null;
  selectedRoom: Room | null = null;
  selectedBed: Bed | null = null;

  // Flags to control form visibility
  showGuestHouseForm: boolean = false;
  showRoomForm: boolean = false;
  showBedForm: boolean = false;

  // For dropdowns in forms
  availableRoomTypes: string[] = ['Single Room', 'Double Room', 'Suite', 'Family Room']; // <--- Added common room types
  availableBedStatuses = Object.values(BedStatus); // Get all values from the enum

  constructor(
    private guesthouseService: GuesthouseService,
    private roomService: RoomService,
    private bedService: BedService
  ) { }

  ngOnInit(): void {
    this.loadAllData();
  }

  // --- Data Loading ---
  loadAllData(): void {
    forkJoin([
      this.guesthouseService.getAllGuestHouses(),
      this.roomService.getAllRooms(),
      this.bedService.getAllBeds()
    ]).subscribe({
      next: ([guesthousesData, roomsData, bedsData]) => {
        this.guesthouses = guesthousesData;
        this.rooms = roomsData;
        this.beds = bedsData;
        console.log('All data loaded:', { guesthouses: guesthousesData, rooms: roomsData, beds: bedsData });
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error loading all data:', error);
        alert('Failed to load all management data. Please try again.');
      }
    });
  }

  // --- GuestHouse CRUD Operations ---
  onAddGuestHouse(): void {
    this.selectedGuestHouse = { name: '', location: '' }; // Initialize empty object for new guesthouse
    this.showGuestHouseForm = true;
    this.showRoomForm = false; // Hide other forms
    this.showBedForm = false;
  }

  onEditGuestHouse(guesthouse: GuestHouse): void {
    this.selectedGuestHouse = { ...guesthouse }; // Create a copy for editing
    this.showGuestHouseForm = true;
    this.showRoomForm = false;
    this.showBedForm = false;
  }

  onSaveGuestHouse(): void {
    if (this.selectedGuestHouse) {
      if (this.selectedGuestHouse.id) {
        // Update existing guesthouse
        this.guesthouseService.updateGuestHouse(this.selectedGuestHouse.id, this.selectedGuestHouse).subscribe({
          next: (updatedGh) => {
            console.log('GuestHouse updated:', updatedGh);
            alert('Guesthouse updated successfully!');
            this.loadAllData();
            this.cancelGuestHouseForm();
          },
          error: (error: HttpErrorResponse) => {
            console.error('Error updating guesthouse:', error);
            alert(`Failed to update guesthouse: ${error.message || 'Server error'}`);
          }
        });
      } else {
        // Create new guesthouse
        this.guesthouseService.createGuestHouse(this.selectedGuestHouse).subscribe({
          next: (newGh) => {
            console.log('GuestHouse created:', newGh);
            alert('Guesthouse created successfully!');
            this.loadAllData();
            this.cancelGuestHouseForm();
          },
          error: (error: HttpErrorResponse) => {
            console.error('Error creating guesthouse:', error);
            alert(`Failed to create guesthouse: ${error.message || 'Server error'}`);
          }
        });
      }
    }
  }

  onDeleteGuestHouse(id: number | undefined): void {
    if (id === undefined) {
      console.warn('Cannot delete guesthouse: ID is undefined.');
      return;
    }
    if (confirm(`Are you sure you want to delete Guesthouse with ID: ${id}? This will also affect associated rooms and beds.`)) {
      this.guesthouseService.deleteGuestHouse(id).subscribe({
        next: (success) => {
          if (success) {
            console.log('Guesthouse deleted successfully');
            alert('Guesthouse deleted successfully!');
            this.loadAllData(); // Reload all data as dependent entities might be affected
          } else {
            console.warn('Guesthouse deletion failed (backend returned false).');
            alert('Guesthouse deletion failed.');
          }
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error deleting guesthouse:', error);
          alert(`Failed to delete guesthouse: ${error.error.message || 'Server error'}. It might have associated rooms/beds.`);
        }
      });
    }
  }

  cancelGuestHouseForm(): void {
    this.selectedGuestHouse = null;
    this.showGuestHouseForm = false;
  }

  // --- Room CRUD Operations ---
  onAddRoom(): void {
    // Make sure a guesthouse is available to link a room to
    if (this.guesthouses.length === 0) {
      alert('Please add a guesthouse first before adding a room.');
      return;
    }
    // Fixed: Initialize all required properties for Room interface
    this.selectedRoom = {
      roomNo: '',
      roomType: this.availableRoomTypes[0] || '', // Default to the first available room type or empty string
      guestHouseId: this.guesthouses[0].id!,
      guestHouseName: this.guesthouses[0].name,
      numberOfBeds: 1,   // <--- Added default value
      pricePerNight: 50  // <--- Added default value, adjust as needed
    };
    this.showRoomForm = true;
    this.showGuestHouseForm = false;
    this.showBedForm = false;
  }

  onEditRoom(room: Room): void {
    this.selectedRoom = { ...room };
    this.showRoomForm = true;
    this.showGuestHouseForm = false;
    this.showBedForm = false;
  }

  onSaveRoom(): void {
    if (this.selectedRoom) {
      // Ensure guestHouseName is set for display purposes (though backend only uses ID)
      const selectedGh = this.guesthouses.find(gh => gh.id === this.selectedRoom?.guestHouseId);
      if (selectedGh) {
        this.selectedRoom.guestHouseName = selectedGh.name;
      } else {
        alert('Please select a valid Guesthouse for the room.');
        return;
      }

      if (this.selectedRoom.id) {
        // Update existing room
        this.roomService.updateRoom(this.selectedRoom.id, this.selectedRoom).subscribe({
          next: (updatedRoom) => {
            console.log('Room updated:', updatedRoom);
            alert('Room updated successfully!');
            this.loadAllData();
            this.cancelRoomForm();
          },
          error: (error: HttpErrorResponse) => {
            console.error('Error updating room:', error);
            alert(`Failed to update room: ${error.message || 'Server error'}`);
          }
        });
      } else {
        // Create new room
        this.roomService.createRoom(this.selectedRoom).subscribe({
          next: (newRoom) => {
            console.log('Room created:', newRoom);
            alert('Room created successfully!');
            this.loadAllData();
            this.cancelRoomForm();
          },
          error: (error: HttpErrorResponse) => {
            console.error('Error creating room:', error);
            alert(`Failed to create room: ${error.message || 'Server error'}`);
          }
        });
      }
    }
  }

  onDeleteRoom(id: number | undefined): void {
    if (id === undefined) {
      console.warn('Cannot delete room: ID is undefined.');
      return;
    }
    if (confirm(`Are you sure you want to delete Room with ID: ${id}? This will also delete associated beds.`)) {
      this.roomService.deleteRoom(id).subscribe({
        next: () => {
          console.log('Room deleted successfully');
          alert('Room deleted successfully!');
          this.loadAllData(); // Reload all data as dependent entities might be affected
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error deleting room:', error);
          alert(`Failed to delete room: ${error.error.message || 'Server error'}. It might have associated beds.`);
        }
      });
    }
  }

  cancelRoomForm(): void {
    this.selectedRoom = null;
    this.showRoomForm = false;
  }

  // --- Bed CRUD Operations ---
  onAddBed(): void {
    // Make sure a room is available to link a bed to
    if (this.rooms.length === 0) {
      alert('Please add a room first before adding a bed.');
      return;
    }
    this.selectedBed = { bedNo: '', status: BedStatus.AVAILABLE, roomId: this.rooms[0].id!, roomNo: this.rooms[0].roomNo }; // Default to first room and AVAILABLE
    this.showBedForm = true;
    this.showGuestHouseForm = false;
    this.showRoomForm = false;
  }

  onEditBed(bed: Bed): void {
    this.selectedBed = { ...bed };
    this.showBedForm = true;
    this.showGuestHouseForm = false;
    this.showRoomForm = false;
  }

  onSaveBed(): void {
    if (this.selectedBed) {
      // Ensure roomNo is set for display purposes (though backend only uses ID)
      const selectedRoom = this.rooms.find(r => r.id === this.selectedBed?.roomId);
      if (selectedRoom) {
        this.selectedBed.roomNo = selectedRoom.roomNo;
      } else {
        alert('Please select a valid Room for the bed.');
        return;
      }

      if (this.selectedBed.id) {
        // Update existing bed
        this.bedService.updateBed(this.selectedBed.id, this.selectedBed).subscribe({
          next: (updatedBed) => {
            console.log('Bed updated:', updatedBed);
            alert('Bed updated successfully!');
            this.loadAllData();
            this.cancelBedForm();
          },
          error: (error: HttpErrorResponse) => {
            console.error('Error updating bed:', error);
            alert(`Failed to update bed: ${error.message || 'Server error'}`);
          }
        });
      } else {
        // Create new bed
        this.bedService.createBed(this.selectedBed).subscribe({
          next: (newBed) => {
            console.log('Bed created:', newBed);
            alert('Bed created successfully!');
            this.loadAllData();
            this.cancelBedForm();
          },
          error: (error: HttpErrorResponse) => {
            console.error('Error creating bed:', error);
            alert(`Failed to create bed: ${error.message || 'Server error'}`);
          }
        });
      }
    }
  }

  onDeleteBed(id: number | undefined): void {
    if (id === undefined) {
      console.warn('Cannot delete bed: ID is undefined.');
      return;
    }
    if (confirm(`Are you sure you want to delete Bed with ID: ${id}?`)) {
      this.bedService.deleteBed(id).subscribe({
        next: (responseMessage) => {
          console.log('Bed deleted successfully:', responseMessage);
          alert('Bed deleted successfully!');
          this.loadAllData(); // Reload all data after deletion
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error deleting bed:', error);
          alert(`Failed to delete bed: ${error.error.message || 'Server error'}`);
        }
      });
    }
  }

  cancelBedForm(): void {
    this.selectedBed = null;
    this.showBedForm = false;
  }
}