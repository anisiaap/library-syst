/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}", // Include your React files
    "./public/index.html",        // Include your HTML files
  ],
  theme: {
    extend: {}, // Extend default Tailwind styles here
  },
  plugins: [],  // Add plugins here if needed
};
