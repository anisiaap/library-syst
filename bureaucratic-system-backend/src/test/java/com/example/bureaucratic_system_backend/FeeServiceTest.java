//package com.example.bureaucratic_system_backend;
//
//import com.example.bureaucratic_system_backend.model.Borrows;
//import com.example.bureaucratic_system_backend.model.Fees;
//import com.example.bureaucratic_system_backend.service.FeeService;
//import com.example.bureaucratic_system_backend.service.FirebaseService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.*;
//
//class FeeServiceTest {
//
//    private FeeService feeService;
//
//    @Mock
//    private FirebaseService firebaseService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        feeService = new FeeService(firebaseService);
//    }
//
//    @Test
//    void testGenerateOverdueFee() {
//        String borrowId = "borrow1";
//
//        Borrows borrow = new Borrows(borrowId, "book1", "membership1", "2024-11-01", "2024-12-01", "2024-12-05");
//
//        // Stubbing getBorrowById method
//        when(firebaseService.getBorrowById(borrowId)).thenReturn(borrow);
//
//        // Stubbing addFee method
//        doNothing().when(firebaseService).addFee(any(Fees.class));
//
//        feeService.generateOverdueFee(borrowId);
//
//        // Verify the method calls
//        verify(firebaseService, times(1)).getBorrowById(borrowId);
//        verify(firebaseService, times(1)).addFee(any(Fees.class));
//    }
//
//    @Test
//    void testGenerateOverdueFeeNoReturnDate() {
//        String borrowId = "borrow1";
//
//        Borrows borrow = new Borrows(borrowId, "book1", "membership1", "2024-11-01", "2024-12-01", null);
//
//        // Stubbing getBorrowById method
//        when(firebaseService.getBorrowById(borrowId)).thenReturn(borrow);
//
//        feeService.generateOverdueFee(borrowId);
//
//        // Verify that no fee was added
//        verify(firebaseService, times(0)).addFee(any(Fees.class));
//    }
//
//    @Test
//    void testMarkFeeAsPaid() {
//        String feeId = "fee1";
//
//        Fees fee = new Fees(feeId, "membership1", "10", "borrow1", "false");
//
//        // Stubbing getFeeByBorrowId method
//        when(firebaseService.getFeeByBorrowId(feeId)).thenReturn(fee);
//        doNothing().when(firebaseService).updateFee(eq(feeId), any(Fees.class));
//
//        feeService.markFeeAsPaid(feeId);
//
//        // Verify that the fee was updated
//        verify(firebaseService, times(1)).getFeeByBorrowId(feeId);
//        verify(firebaseService, times(1)).updateFee(eq(feeId), any(Fees.class));
//    }
//
//    @Test
//    void testMarkFeeAsPaidNotFound() {
//        String feeId = "fee1";
//
//        // Stubbing getFeeByBorrowId method to return null (fee not found)
//        when(firebaseService.getFeeByBorrowId(feeId)).thenReturn(null);
//
//        feeService.markFeeAsPaid(feeId);
//
//        // Verify that the update method was not called
//        verify(firebaseService, times(0)).updateFee(eq(feeId), any(Fees.class));
//    }
//
//    @Test
//    void testDeleteFee() {
//        String feeId = "fee1";
//
//        // Stubbing deleteFee method
//        doNothing().when(firebaseService).deleteFee(feeId);
//
//        feeService.deleteFee(feeId);
//
//        // Verify that the deleteFee method was called
//        verify(firebaseService, times(1)).deleteFee(feeId);
//    }
//
//    @Test
//    void testGetFeeByBorrowId() {
//        String borrowId = "borrow1";
//
//        Fees fee = new Fees("fee1", "membership1", "10", borrowId, "false");
//
//        // Stubbing getFeeByBorrowId method
//        when(firebaseService.getFeeByBorrowId(borrowId)).thenReturn(fee);
//
//        Fees result = feeService.getFeeByBorrowId(borrowId);
//
//        // Verify that the fee was retrieved and not null
//        verify(firebaseService, times(1)).getFeeByBorrowId(borrowId);
//        assert result != null;
//        assert result.getBorrowId().equals(borrowId);
//    }
//
//    @Test
//    void testGetFeeByBorrowIdNotFound() {
//        String borrowId = "borrow1";
//
//        // Stubbing getFeeByBorrowId method to return null
//        when(firebaseService.getFeeByBorrowId(borrowId)).thenReturn(null);
//
//        Fees result = feeService.getFeeByBorrowId(borrowId);
//
//        // Verify that the result is null
//        verify(firebaseService, times(1)).getFeeByBorrowId(borrowId);
//        assert result == null;
//    }
//}