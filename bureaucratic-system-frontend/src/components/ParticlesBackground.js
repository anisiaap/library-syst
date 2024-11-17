import React, { useEffect, useRef } from 'react';

const ParticlesBackground = () => {
  const particlesRef = useRef(null);

  useEffect(() => {
    const initializeParticles = () => {
      if (typeof window !== 'undefined' && window.particlesJS && particlesRef.current) {
        window.particlesJS('particles-container', {
          particles: {
            number: {
              value: 150, // Number of particles
              density: { enable: true, value_area: 800 },
            },
            color: { value: '#ffffff' }, // White particles
            shape: { type: 'circle', stroke: { width: 0, color: '#000000' } },
            opacity: { value: 0.6, random: true },
            size: { value: 3, random: true },
            line_linked: { enable: true, distance: 120, color: '#ffffff', opacity: 0.4, width: 1 },
            move: { enable: true, speed: 2, direction: 'none', random: true, out_mode: 'out' },
          },
          interactivity: {
            detect_on: 'canvas',
            events: {
              onhover: { enable: true, mode: 'grab' },
              onclick: { enable: true, mode: 'push' },
              resize: true,
            },
            modes: {
              grab: { distance: 180, line_linked: { opacity: 0.7 } },
              repulse: { distance: 200, duration: 0.4 },
            },
          },
          retina_detect: true,
        });
      } else {
        setTimeout(initializeParticles, 300);
      }
    };

    initializeParticles();
  }, []);

  return (
    <div
      id="particles-container"
      ref={particlesRef}
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: '#1a202c', // Tailwind dark gray
        zIndex: -1,
        pointerEvents: 'none', // Allows interactions through the particles
      }}
    />
  );
};

export default ParticlesBackground;
