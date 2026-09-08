import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    // BFF_INTERNAL_URL is set when running inside Docker (http://bff-service:8090).
    // Falls back to localhost for local development.
    const bffUrl = process.env.BFF_INTERNAL_URL ?? "http://localhost:8090";
    return [
      {
        source: "/api/:path*",
        destination: `${bffUrl}/api/:path*`,
      },
      {
        source: "/ws",
        destination: `${bffUrl}/ws`,
      },
    ];
  },
};

export default nextConfig;
