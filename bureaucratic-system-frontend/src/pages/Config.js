import React, { useState } from 'react';
import axios from 'axios';

const Config = () => {
  const [config, setConfig] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    axios.post('http://localhost:8080/api/admin/config', JSON.parse(config))
      .then(() => alert('Configuration submitted!'))
      .catch(err => alert('Error: ' + err.message));
  };

  return (
    <form onSubmit={handleSubmit}>
      <h1>Configure Offices</h1>
      <textarea placeholder="Configuration JSON" value={config} onChange={e => setConfig(e.target.value)} required />
      <button type="submit">Submit</button>
    </form>
  );
};

export default Config;
