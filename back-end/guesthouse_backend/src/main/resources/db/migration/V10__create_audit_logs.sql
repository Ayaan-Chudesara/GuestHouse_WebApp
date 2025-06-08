-- Create the audit_logs table
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT,
    action_type VARCHAR(50) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    additional_info TEXT,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Create trigger for INSERT operations
DELIMITER //
CREATE TRIGGER booking_insert_trigger
AFTER INSERT ON bookings
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (booking_id, user_id, action_type, new_status, additional_info)
    VALUES (NEW.id, NEW.user_id, 'INSERT', NEW.status, 
            CONCAT('New booking created for user ', NEW.user_id, 
                   ' with check-in date ', NEW.booking_date));
END;
//

-- Create trigger for UPDATE operations
CREATE TRIGGER booking_update_trigger
AFTER UPDATE ON bookings
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO audit_logs (booking_id, user_id, action_type, old_status, new_status, additional_info)
        VALUES (NEW.id, NEW.user_id, 'UPDATE', OLD.status, NEW.status,
                CONCAT('Booking status changed from ', OLD.status, ' to ', NEW.status));
    END IF;
END;
//

-- Create trigger for DELETE operations
CREATE TRIGGER booking_delete_trigger
BEFORE DELETE ON bookings
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (booking_id, user_id, action_type, old_status, additional_info)
    VALUES (OLD.id, OLD.user_id, 'DELETE', OLD.status,
            CONCAT('Booking deleted with status ', OLD.status));
END;
//

DELIMITER ; 