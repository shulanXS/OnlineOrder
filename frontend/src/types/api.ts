/**
 * 统一 API 类型定义。
 * 与后端 DTO 保持字段名称一致。
 */

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface MenuItem {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
}

export interface Restaurant {
  id: number;
  name: string;
  address: string;
  phone: string;
  imageUrl: string;
  menuItems: MenuItem[];
}

export interface CartItem {
  cartItemId: number;
  menuItemId: number;
  restaurantId: number;
  price: number;
  quantity: number;
  menuItemName: string;
  menuItemDescription: string;
  menuItemImageUrl: string;
}

export interface Cart {
  id: number | null;
  totalPrice: number;
  cartItems: CartItem[];
}

export interface OrderDetail {
  id: number;
  menuItemId: number;
  menuItemName: string;
  menuItemDescription: string;
  menuItemImageUrl: string;
  price: number;
  quantity: number;
}

export interface Order {
  id: number;
  customerId: number;
  status: string;
  totalPrice: number;
  createdAt: string;
  updatedAt: string;
  details: OrderDetail[];
}

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}
