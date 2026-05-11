/**
 * 订单历史页面。
 *
 * 展示用户所有订单的列表，支持折叠展开查看明细。
 *
 * 状态管理：
 * - useOrders() 获取订单数据
 * - 本地 useMemo 缓存格式化后的订单列表，避免重复计算
 *
 * 订单状态展示：
 * - 每个状态对应一个固定的颜色和中文标签（STATUS_MAP）
 * - 未知状态默认回退到 PENDING（兜底展示）
 * - 颜色方案符合 Ant Design Tag 的语义化配色
 *
 * 订单列表设计：
 * - 使用 Collapse 手风琴布局，节省页面空间
 * - 订单按创建时间倒序排列（后端已排序）
 * - 展开后展示商品明细和数量
 *
 * 日期格式化：
 * - 使用 Intl.DateTimeFormat 或 Date.toLocaleString 转换为本地可读格式
 * - 保留分钟级精度
 */
import { Collapse, Empty, Spin, Typography, Tag } from 'antd';
import { useOrders } from '../hooks/useOrders';
import { formatPrice, formatDate } from '../utils/formatters';

const { Text, Title } = Typography;
const { Panel } = Collapse;

/**
 * 订单状态到 Ant Design Tag 颜色的映射。
 *
 * 设计说明：
 * - orange: 待支付（PENDING）—— 警示感，提示用户行动
 * - blue: 已确认（CONFIRMED）—— 冷静蓝，表示已开始处理
 * - processing: 制作中（PREPARING）—— 带动画processing色，表示进行中
 * - cyan: 配送中（SHIPPING）—— 蓝绿色，表示在路上
 * - green: 已完成（COMPLETED）—— 绿色表示成功
 * - red: 已取消（CANCELLED）—— 红色表示终止
 *
 * 注意：如果后端增加了新状态，此 Map 需要同步更新。
 * TypeScript 的 Record<string, ...> 类型允许动态 key，
 * 但未知状态会回退到 PENDING 的样式。
 */
const STATUS_MAP: Record<string, { color: string; text: string }> = {
  PENDING:    { color: 'orange',    text: '待支付' },
  CONFIRMED:  { color: 'blue',     text: '已确认' },
  PREPARING:  { color: 'processing', text: '制作中' },
  SHIPPING:   { color: 'cyan',    text: '配送中' },
  COMPLETED:  { color: 'green',   text: '已完成' },
  CANCELLED:  { color: 'red',     text: '已取消' },
};

const OrdersPage = () => {
  const { data: orders, isLoading, error } = useOrders();

  if (isLoading) return <div style={{ textAlign: 'center', marginTop: 100 }}><Spin size="large" /></div>;
  if (error) return <Empty description={<Text type="danger">{error.message}</Text>} />;

  const list = Array.isArray(orders) ? orders : [];

  return (
    <div>
      <Title level={3}>我的订单</Title>

      {list.length === 0 ? (
        <Empty description="暂无订单记录" style={{ marginTop: 60 }} />
      ) : (
        <Collapse bordered={false} style={{ marginTop: 16 }}>
          {list.map((order) => {
            // 未知状态回退到 PENDING 的样式，确保 UI 不会崩溃
            const cfg = STATUS_MAP[order.status] || STATUS_MAP.PENDING;
            return (
              <Panel
                key={order.id}
                header={
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      width: '100%',
                      paddingRight: 16,
                    }}
                  >
                    <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                      <Text strong>订单 #{order.id}</Text>
                      <Tag color={cfg.color}>{cfg.text}</Tag>
                    </div>
                    <div style={{ display: 'flex', gap: 24, alignItems: 'center' }}>
                      <Text type="secondary">{formatDate(order.createdAt)}</Text>
                      <Text strong>{formatPrice(order.totalPrice)}</Text>
                    </div>
                  </div>
                }
              >
                {(order.details ?? []).map((d) => (
                  <div
                    key={d.id}
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      padding: '6px 0',
                      borderBottom: '1px solid #f0f0f0',
                    }}
                  >
                    <Text>{d.menuItemName}</Text>
                    <Text type="secondary">{formatPrice(d.price)} x {d.quantity}</Text>
                  </div>
                ))}
                {(!order.details || order.details.length === 0) && (
                  <Text type="secondary">无商品明细</Text>
                )}
              </Panel>
            );
          })}
        </Collapse>
      )}
    </div>
  );
};

export default OrdersPage;
