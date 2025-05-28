ALTER TABLE bookings
DROP FOREIGN KEY FKeyog2oic85xg7hsu2je2lx3s6,
ADD CONSTRAINT FK_bookings_user 
FOREIGN KEY (user_id) REFERENCES user(id); 