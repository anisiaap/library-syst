//package com.example.bureaucratic_system_backend;
//
//import com.example.bureaucratic_system_backend.model.Book;
//import com.example.bureaucratic_system_backend.model.Borrows;
//import com.example.bureaucratic_system_backend.model.Citizen;
//import com.example.bureaucratic_system_backend.model.Membership;
//import com.example.bureaucratic_system_backend.service.AdminService;
//import com.example.bureaucratic_system_backend.service.FirebaseService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.*;
//
//public class AdminServiceTest {
//
//    private AdminService adminService;
//
//    @Mock
//    private FirebaseService firebaseService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        adminService = new AdminService(firebaseService);
//    }
//
//    // ----------------------- Books Management Tests -----------------------
//
//    @Test
//    void testAddBook() {
//        Book book = new Book("book1", "Test Book", "Test Author", true);
//        doNothing().when(firebaseService).addBook(book);
//
//        adminService.addBook(book);
//
//        verify(firebaseService, times(1)).addBook(book);
//    }
//
//    @Test
//    void testUpdateBookField() {
//        String bookId = "book1";
//        String fieldName = "name";
//        String newValue = "Updated Title";
//
//        doNothing().when(firebaseService).updateField("books", bookId, fieldName, newValue);
//
//        adminService.updateBookField(bookId, fieldName, newValue);
//
//        verify(firebaseService, times(1)).updateField("books", bookId, fieldName, newValue);
//    }
//
//    @Test
//    void testDeleteBook() {
//        String bookId = "book1";
//
//        doNothing().when(firebaseService).deleteBook(bookId);
//
//        adminService.deleteBook(bookId);
//
//        verify(firebaseService, times(1)).deleteBook(bookId);
//    }
//
//    // ----------------------- Citizens Management Tests -----------------------
//
//    @Test
//    void testAddCitizen() {
//        Citizen citizen = new Citizen("citizen1", "John Doe");
//        doNothing().when(firebaseService).addCitizen(citizen);
//
//        adminService.addCitizen(citizen);
//
//        verify(firebaseService, times(1)).addCitizen(citizen);
//    }
//
//    @Test
//    void testUpdateCitizenField() {
//        String citizenId = "citizen1";
//        String fieldName = "name";
//        String newValue = "Jane Doe";
//
//        doNothing().when(firebaseService).updateField("citizens", citizenId, fieldName, newValue);
//
//        adminService.updateCitizenField(citizenId, fieldName, newValue);
//
//        verify(firebaseService, times(1)).updateField("citizens", citizenId, fieldName, newValue);
//    }
//
//    @Test
//    void testDeleteCitizen() {
//        String citizenId = "citizen1";
//
//        doNothing().when(firebaseService).deleteCitizen(citizenId);
//
//        adminService.deleteCitizen(citizenId);
//
//        verify(firebaseService, times(1)).deleteCitizen(citizenId);
//    }
//
//    // ----------------------- Memberships Management Tests -----------------------
//
//    @Test
//    void testAddMembership() {
//        Membership membership = new Membership("membership1", "2024-11-01", "citizen1");
//        doNothing().when(firebaseService).addMembership(membership);
//
//        adminService.addMembership(membership);
//
//        verify(firebaseService, times(1)).addMembership(membership);
//    }
//
//    @Test
//    void testUpdateMembershipField() {
//        String membershipId = "membership1";
//        String fieldName = "issueDate";
//        String newValue = "2024-11-15";
//
//        doNothing().when(firebaseService).updateField("memberships", membershipId, fieldName, newValue);
//
//        adminService.updateMembershipField(membershipId, fieldName, newValue);
//
//        verify(firebaseService, times(1)).updateField("memberships", membershipId, fieldName, newValue);
//    }
//
//    @Test
//    void testDeleteMembership() {
//        String membershipId = "membership1";
//
//        doNothing().when(firebaseService).deleteMembership(membershipId);
//
//        adminService.deleteMembership(membershipId);
//
//        verify(firebaseService, times(1)).deleteMembership(membershipId);
//    }
//
//    // ----------------------- Fees Management Tests -----------------------
//
//    @Test
//    void testUpdateFeeField() {
//        String feeId = "fee1";
//        String fieldName = "amount";
//        String newValue = "20";
//
//        doNothing().when(firebaseService).updateField("fees", feeId, fieldName, newValue);
//
//        adminService.updateFeeField(feeId, fieldName, newValue);
//
//        verify(firebaseService, times(1)).updateField("fees", feeId, fieldName, newValue);
//    }
//
//    @Test
//    void testDeleteFee() {
//        String feeId = "fee1";
//
//        doNothing().when(firebaseService).deleteFee(feeId);
//
//        adminService.deleteFee(feeId);
//
//        verify(firebaseService, times(1)).deleteFee(feeId);
//    }
//
//    // ----------------------- Borrows Management Tests -----------------------
//
//    @Test
//    void testAddBorrow() {
//        Borrows borrow = new Borrows("borrow1", "book1", "membership1", "2024-11-01", "2024-12-01", null);
//        doNothing().when(firebaseService).addBorrow(borrow);
//
//        adminService.addBorrow(borrow);
//
//        verify(firebaseService, times(1)).addBorrow(borrow);
//    }
//
//    @Test
//    void testUpdateBorrow() {
//        String borrowId = "borrow1";
//        Borrows updatedBorrow = new Borrows("borrow1", "book1", "membership1", "2024-11-01", "2024-12-01", "2024-11-30");
//
//        doNothing().when(firebaseService).updateBorrow(borrowId, updatedBorrow);
//
//        adminService.updateBorrow(borrowId, updatedBorrow);
//
//        verify(firebaseService, times(1)).updateBorrow(borrowId, updatedBorrow);
//    }
//
//    @Test
//    void testDeleteBorrow() {
//        String borrowId = "borrow1";
//
//        doNothing().when(firebaseService).deleteBorrow(borrowId);
//
//       // adminService.deleteBorrow(borrowId);
//
//        verify(firebaseService, times(1)).deleteBorrow(borrowId);
//    }
//}