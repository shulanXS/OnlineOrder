/**
 * 全局布局组件。
 *
 * 包含固定 Header 和可滚动 Content 区域。
 * Header 中根据登录状态显示不同操作按钮。
 *
 * 设计要点：
 * - 购物车 Badge 数量：仅在已登录时调用 useCart()，避免未认证时产生 401 请求
 * - 退出按钮点击后弹出确认框，防止误操作
 * - Header 使用 sticky 定位，滚动时始终可见
 */

import { Link, useNavigate } from 'react-router-dom';
import { Badge, Button, Layout, Modal, Typography } from 'antd';
import { ShoppingCartOutlined, HistoryOutlined, LogoutOutlined } from '@ant-design/icons';
import { useAuthStore } from '../stores/authStore';
import { useCart } from '../hooks/useCart';

const { Header, Content } = Layout;
const { Title } = Typography;

const LayoutComponent = ({ children }: { children: React.ReactNode }) => {
  const navigate = useNavigate();
  // isAuthenticated 从 token 派生，避免显式存储导致的状态不一致
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const logout = useAuthStore((s) => s.logout);

  // 仅在已登录时加载购物车数据，避免产生不必要的 401 请求
  const { data: cart } = isAuthenticated ? useCart() : { data: undefined };

  const handleLogout = () => {
    Modal.confirm({
      title: '确认退出',
      content: '确定要退出登录吗？',
      okText: '确认退出',
      cancelText: '取消',
      onOk: () => {
        logout();
        navigate('/login');
      },
    });
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          position: 'sticky',
          top: 0,
          zIndex: 100,
        }}
      >
        <Link to="/restaurants" style={{ textDecoration: 'none' }}>
          <Title
            level={3}
            className="site-header-title"
            style={{ color: 'white', marginBottom: 0, cursor: 'pointer' }}
          >
            Lai Food
          </Title>
        </Link>

        <div className="header-buttons" style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          {isAuthenticated ? (
            <>
              <Button
                type="default"
                shape="round"
                icon={<HistoryOutlined />}
                onClick={() => navigate('/orders')}
              >
                我的订单
              </Button>
              <Badge count={cart?.cartItems?.length || 0} size="small" offset={[5, -5]}>
                <Button
                  type="primary"
                  shape="round"
                  icon={<ShoppingCartOutlined />}
                  onClick={() => navigate('/cart')}
                >
                  购物车
                </Button>
              </Badge>
              <Button
                type="text"
                shape="round"
                icon={<LogoutOutlined />}
                onClick={handleLogout}
                style={{ color: '#ffffffb3' }}
              >
                退出
              </Button>
            </>
          ) : (
            <>
              <Button shape="round" onClick={() => navigate('/login')}>
                登录
              </Button>
              <Button type="primary" shape="round" onClick={() => navigate('/register')}>
                注册
              </Button>
            </>
          )}
        </div>
      </Header>

      <Content
        style={{
          padding: '32px 50px',
          maxWidth: 1200,
          margin: '0 auto',
          width: '100%',
        }}
      >
        {children}
      </Content>
    </Layout>
  );
};

export default LayoutComponent;
