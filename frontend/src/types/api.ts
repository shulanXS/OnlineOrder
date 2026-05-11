/**
 * 统一 API 类型定义。
 *
 * 与后端 DTO 保持字段名称一致（camelCase）。
 * 此文件是前后端数据契约的唯一真实来源。
 *
 * 设计原则：
 * - 每个接口都对应后端的一个 DTO 类
 * - 枚举/受限值使用 TypeScript union type 提供类型安全
 * - 避免 any，使用 unknown 配合类型守卫
 *
 * 与后端 DTO 的对应关系：
 * - MenuItem         <-> MenuItemDto
 * - Restaurant       <-> RestaurantDto (嵌套结构)
 * - Cart             <-> CartDto
 * - CartItem         <-> CartItemDto
 * - Order            <-> OrderDto
 * - OrderDetail      <-> OrderDetailDto
 * - AuthResponse     <-> AuthResponse
 * - LoginCredentials <-> LoginRequest
 * - RegisterData     <-> RegisterBody
 * - User             <-> CustomerDto
 */

/** 餐厅菜品。 */
export interface MenuItem {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
}

/** 餐厅（含菜品列表）。注意：menuItems 是嵌套结构，菜品在 RestaurantDto 内部。 */
export interface Restaurant {
  id: number;
  name: string;
  address: string;
  phone: string;
  imageUrl: string;
  menuItems: MenuItem[];
}

/** 购物车中的一件商品。 */
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

/** 购物车整体。id 为 null 表示尚未持久化（新建购物车）。 */
export interface Cart {
  id: number | null;
  totalPrice: number;
  cartItems: CartItem[];
}

/** 订单明细（一件已购买的商品）。 */
export interface OrderDetail {
  id: number;
  /** 后端会返回此字段但前端不消费，保留用于调试。 */
  menuItemId: number;
  menuItemName: string;
  menuItemDescription: string;
  menuItemImageUrl: string;
  price: number;
  quantity: number;
}

/**
 * 订单状态枚举。
 * 对应后端 OrderEntity.status：PENDING / CONFIRMED / PREPARING / SHIPPING / COMPLETED / CANCELLED。
 *
 * 注意：后端目前用 String 存储此值，此类型提供了 IDE 级类型安全和编译器提示。
 * 若后端扩展新状态，TypeScript 会在编译时报错，强制更新此类型定义。
 */
export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PREPARING'
  | 'SHIPPING'
  | 'COMPLETED'
  | 'CANCELLED';

/** 订单。 */
export interface Order {
  id: number;
  customerId: number;
  /** 订单状态。使用 OrderStatus union type 而非 string 以获得类型安全。 */
  status: OrderStatus | string;
  totalPrice: number;
  createdAt: string;
  updatedAt: string;
  details: OrderDetail[];
}

/** 登录凭证。 */
export interface LoginCredentials {
  /** 邮箱地址（后端同时支持 username 字段名，前端与后端 DTO 字段名一致）。 */
  email: string;
  password: string;
}

/** 注册信息。 */
export interface RegisterData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

/** JWT Token 响应。 */
export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

/** 当前登录用户信息。 */
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
}
