//package com.example.bureaucratic_system_backend;
//
//import com.example.bureaucratic_system_backend.model.Borrows;
//import com.example.bureaucratic_system_backend.service.BorrowService;
//import com.example.bureaucratic_system_backend.service.FirebaseService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.*;
//
//class BorrowServiceTest {
//
//    private BorrowService borrowService;
//
//    @Mock
//    private FirebaseService firebaseService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        borrowService = new BorrowService(firebaseService);
//    }
//
//    @Test
//    void testCreateBorrow() {
//        String borrowId = "borrow1";
//        String bookId = "book1";
//        String membershipId = "membership1";
//
//        // Stubbing the addBorrow method
//        doNothing().when(firebaseService).addBorrow(any(Borrows.class));
//
//        borrowService.createBorrow(borrowId, bookId, membershipId);
//
//        // Verify that addBorrow was called
//        verify(firebaseService, times(1)).addBorrow(any(Borrows.class));
//    }
//
//    @Test
//    void testUpdateReturnDate() {
//        String borrowId = "borrow1";
//        String returnDate = "2024-12-25";
//
//        Borrows borrow = new Borrows(borrowId, "book1", "membership1", "2024-11-20", "2024-12-20", null);
//
//        // Stubbing getBorrowById and updateBorrow methods
//        when(firebaseService.getBorrowById(borrowId)).thenReturn(borrow);
//        doNothing().when(firebaseService).updateBorrow(eq(borrowId), any(Borrows.class));
//
//        borrowService.updateReturnDate(borrowId, returnDate);
//
//        // Verify that the return date was updated
//        verify(firebaseService, times(1)).getBorrowById(borrowId);
//        verify(firebaseService, times(1)).updateBorrow(eq(borrowId), any(Borrows.class));
//    }
//
//    @Test
//    void testGetBorrowById() {
//        String borrowId = "borrow1";
//
//        Borrows borrow = new Borrows(borrowId, "book1", "membership1", "2024-11-20", "2024-12-20", null);
//
//        // Stubbing getBorrowById method
//        when(firebaseService.getBorrowById(borrowId)).thenReturn(borrow);
//
//        Borrows result = borrowService.getBorrowById(borrowId);
//
//        // Verify the method call and the result
//        verify(firebaseService, times(1)).getBorrowById(borrowId);
//        assert result != null;
//        assert result.getId().equals(borrowId);
//    }
//
//    @Test
//    void testDeleteBorrow() {
//        String borrowId = "borrow1";
//
//        doNothing().when(firebaseService).deleteBorrow(borrowId);
//
//        borrowService.deleteBorrow(borrowId);
//
//        verify(firebaseService, times(1)).deleteBorrow(borrowId);
//    }
//}