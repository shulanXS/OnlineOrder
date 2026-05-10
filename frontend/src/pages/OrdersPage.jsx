/**
 * 订单历史页面。
 * 展示用户的所有订单。
 */
import { Collapse, Empty, Spin, Typography, Tag } from 'antd';
import { useOrders } from '../hooks/useOrders';

const { Text, Title } = Typography;
const { Panel } = Collapse;

const STATUS_MAP = {
  PENDING: { color: 'orange', text: '待支付' },
  CONFIRMED: { color: 'blue', text: '已确认' },
  PREPARING: { color: 'processing', text: '制作中' },
  SHIPPING: { color: 'cyan', text: '配送中' },
  COMPLETED: { color: 'success', text: '已完成' },
  CANCELLED: { color: 'error', text: '已取消' },
};

const formatDate = (d) => new Date(d).toLocaleString('zh-CN', {
  year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
});

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
          {list.map(order => {
            const cfg = STATUS_MAP[order.status] || STATUS_MAP.PENDING;
            return (
              <Panel key={order.id} header={
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%', paddingRight: 16 }}>
                  <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                    <Text strong>订单 #{order.id}</Text>
                    <Tag color={cfg.color}>{cfg.text}</Tag>
                  </div>
                  <div style={{ display: 'flex', gap: 24, alignItems: 'center' }}>
                    <Text type="secondary">{formatDate(order.createdAt || order.created_at)}</Text>
                    <Text strong>${order.totalPrice?.toFixed(2) || 0}</Text>
                  </div>
                </div>
              }>
                {(order.details || []).map(d => (
                  <div key={d.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid #f0f0f0' }}>
                    <Text>{d.menuItemName || d.menu_item_name}</Text>
                    <Text type="secondary">${d.price} x {d.quantity}</Text>
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
