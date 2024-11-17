import React, { useState } from 'react';
import axios from 'axios';

const Enroll = () => {
  const [id, setId] = useState('');
  const [name, setName] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    axios.post('http://localhost:8080/api/enroll', { id, name })
      .then(() => alert('Citizen enrolled!'))
      .catch(err => alert('Error: ' + err.message));
  };

  return (
    <form onSubmit={handleSubmit}>
      <h1>Enroll Citizen</h1>
      <input
        placeholder="ID"
        value={id}
        onChange={e => setId(e.target.value)}
        required
      />
      <input
        placeholder="Name"
        value={name}
        onChange={e => setName(e.target.value)}
        required
      />
      <button type="submit">Enroll</button>
    </form>
  );
};

export default Enroll;
