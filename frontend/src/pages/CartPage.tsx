/**
 * 购物车页面。
 * 展示购物车内容，支持修改数量和结账。
 *
 * 状态管理：
 * - useCart() 读取购物车数据
 * - useUpdateQuantity() 更新商品数量（含乐观更新）
 * - useCheckout() 结账（自动清空购物车并刷新订单）
 *
 * 乐观更新（Optimistic Update）：
 * - updateItemQuantity 在请求发出前直接更新 UI，提升响应速度
 * - 若请求失败，onError 回调会回滚到之前的状态
 *
 * 结账流程：
 * 1. 弹出确认框显示金额
 * 2. 用户确认后调用 checkout API
 * 3. 成功后清空本地购物车状态、刷新订单列表、跳转订单页
 */
import { Button, Empty, InputNumber, List, Modal, Spin, Typography, message } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useCart, useUpdateQuantity, useCheckout } from '../hooks/useCart';
import { useApiError } from '../hooks/useApiError';
import { CURRENCY_SYMBOL } from '../constants';

const { Text, Title } = Typography;

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60"%3E%3Crect fill="%23f0f0f0" width="60" height="60"/%3E%3Ctext x="50%25" y="50%25" font-family="sans-serif" font-size="10" fill="%23999" text-anchor="middle" dy=".3em"%3ENo%3C/text%3E%3C/svg%3E';

/** 格式化金额为字符串（保留两位小数）。 */
const formatPrice = (price: number | string | null | undefined): string => {
  const n = Number(price) || 0;
  return `${CURRENCY_SYMBOL}${n.toFixed(2)}`;
};

const CartPage = () => {
  const navigate = useNavigate();
  const { data: cart, isLoading, error } = useCart();
  const { mutate: updateQuantity } = useUpdateQuantity();
  const { mutate: checkout, isPending: isCheckingOut } = useCheckout();
  const { showError } = useApiError();

  if (isLoading) {
    return <div style={{ textAlign: 'center', marginTop: 100 }}><Spin size="large" /></div>;
  }
  if (error) return <Empty description={<Text type="danger">{error.message}</Text>} />;

  const items = cart?.cartItems || [];

  const handleCheckout = () => {
    Modal.confirm({
      title: '确认结账',
      content: `合计：${formatPrice(cart?.totalPrice)}，确认提交订单？`,
      okText: '确认结账',
      cancelText: '取消',
      onOk: () => {
        checkout(undefined, {
          onSuccess: () => {
            message.success('结账成功！');
            navigate('/orders');
          },
          onError: showError,
        });
      },
    });
  };

  return (
    <div>
      <Title level={3}>我的购物车</Title>

      {items.length === 0 ? (
        <Empty description="购物车是空的，快去选购吧！" style={{ marginTop: 60 }}>
          <Button type="primary" onClick={() => navigate('/restaurants')}>
            去浏览餐厅
          </Button>
        </Empty>
      ) : (
        <>
          <List style={{ marginTop: 16 }} itemLayout="horizontal" dataSource={items}
            renderItem={(item) => (
              <List.Item
                actions={[
                  <InputNumber
                    key="qty"
                    min={0}
                    max={99}
                    value={item.quantity}
                    onChange={(val) =>
                      updateQuantity({ menuItemId: item.menuItemId, quantity: val ?? 0 })
                    }
                    style={{ width: 70 }}
                  />,
                  <Button
                    key="del"
                    type="text"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={() =>
                      updateQuantity({ menuItemId: item.menuItemId, quantity: 0 })
                    }
                  />,
                ]}
              >
                <List.Item.Meta
                  avatar={
                    <img
                      src={item.menuItemImageUrl}
                      alt={item.menuItemName}
                      onError={(e) => { (e.target as HTMLImageElement).src = PLACEHOLDER_IMAGE; }}
                      style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4 }}
                    />
                  }
                  title={item.menuItemName}
                  description={
                    <Text>
                      {formatPrice(item.price)} x {item.quantity} = {formatPrice(Number(item.price) * item.quantity)}
                    </Text>
                  }
                />
              </List.Item>
            )}
          />

          <div
            style={{
              position: 'sticky',
              bottom: 0,
              background: '#fff',
              borderTop: '1px solid #f0f0f0',
              padding: '16px 0',
              marginTop: 16,
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <div>
              <Text type="secondary">共 {items.length} 件商品</Text>
              <Title level={3} style={{ margin: '4px 0 0' }}>
                总计：{formatPrice(cart?.totalPrice)}
              </Title>
            </div>
            <Button type="primary" size="large" onClick={handleCheckout} loading={isCheckingOut}>
              结算
            </Button>
          </div>
        </>
      )}
    </div>
  );
};

export default CartPage;
