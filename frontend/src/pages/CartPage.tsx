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
 *
 * 数量变更 UX：
 * - InputNumber 最小值为 0，达到 0 时自动触发删除逻辑
 * - 删除按钮点击后直接调用数量归零，简化交互
 * - 乐观更新确保 UI 即时响应，无需等待网络返回
 */
import { Button, Empty, InputNumber, List, Modal, Spin, Typography, message } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useCart, useUpdateQuantity, useCheckout } from '../hooks/useCart';
import { useApiError } from '../hooks/useApiError';
import { PLACEHOLDER_IMAGE } from '../constants';
import { formatPrice } from '../utils/formatters';

const { Text, Title } = Typography;

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
                    onClick={() => {
                      // 删除前弹出确认，避免误触
                      Modal.confirm({
                        title: '确认移除',
                        content: `确定要从购物车移除「${item.menuItemName}」吗？`,
                        okText: '移除',
                        cancelText: '取消',
                        okButtonProps: { danger: true },
                        onOk: () => {
                          updateQuantity({ menuItemId: item.menuItemId, quantity: 0 });
                          message.success('已从购物车移除');
                        },
                      });
                    }}
                  />,
                ]}
              >
                <List.Item.Meta
                  avatar={
                    <img
                      src={item.menuItemImageUrl}
                      alt={item.menuItemName}
                      onError={(e) => { (e.target as HTMLImageElement).src = PLACEHOLDER_IMAGE.cart; }}
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
