import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Books = () => {
  const [books, setBooks] = useState([]);

  useEffect(() => {
    axios.get('http://localhost:8080/api/books')
      .then(response => setBooks(response.data))
      .catch(error => console.error('Error fetching books:', error));
  }, []);

  return (
    <div>
      <h1>Books</h1>
      <ul>
        {books.map(book => (
          <li key={book.id}>{book.title}</li>
        ))}
      </ul>
    </div>
  );
};

export default Books;
