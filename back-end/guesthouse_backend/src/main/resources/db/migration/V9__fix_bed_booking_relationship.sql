-- Drop the existing foreign key and unique constraint
ALTER TABLE bookings
DROP FOREIGN KEY FK_booking_bed;

-- Drop the unique index
ALTER TABLE bookings
DROP INDEX UKeg6uh7aikbuiu9h7dwp6ljnbn;

-- Recreate the foreign key without unique constraint
ALTER TABLE bookings
ADD CONSTRAINT FK_booking_bed
FOREIGN KEY (bed_id) REFERENCES beds(id); 