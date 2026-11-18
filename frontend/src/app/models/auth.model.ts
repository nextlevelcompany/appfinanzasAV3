export interface AuthRequest {
  username: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  username: string;
}
