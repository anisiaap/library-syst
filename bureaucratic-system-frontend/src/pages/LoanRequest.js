import React, { useState } from 'react';
import axios from 'axios';

const LoanRequest = () => {
  const [citizenId, setCitizenId] = useState('');
  const [bookTitle, setBookTitle] = useState('');
  const [bookAuthor, setBookAuthor] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    axios.post('http://localhost:8080/api/loan-request', {
      citizenId,
      bookTitle,
      bookAuthor,
    }).then(() => alert('Request submitted!'))
      .catch(err => alert('Error: ' + err.message));
  };

  return (
    <form onSubmit={handleSubmit}>
      <h1>Loan Request</h1>
      <input placeholder="Citizen ID" value={citizenId} onChange={e => setCitizenId(e.target.value)} required />
      <input placeholder="Book Title" value={bookTitle} onChange={e => setBookTitle(e.target.value)} required />
      <input placeholder="Book Author" value={bookAuthor} onChange={e => setBookAuthor(e.target.value)} required />
      <button type="submit">Submit</button>
    </form>
  );
};

export default LoanRequest;
