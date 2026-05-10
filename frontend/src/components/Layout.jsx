/**
 * 全局布局组件。
 *
 * 包含固定 Header 和可滚动 Content 区域。
 * Header 中根据登录状态显示不同操作按钮。
 */

import { Link, useNavigate } from 'react-router-dom';
import { Button, Layout, Typography } from 'antd';
import { ShoppingCartOutlined, HistoryOutlined, LogoutOutlined } from '@ant-design/icons';
import { useAuthStore } from '../stores/authStore';

const { Header, Content } = Layout;
const { Title } = Typography;

const LayoutComponent = ({ children }) => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    navigate('/login');
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
            style={{ color: 'white', marginBottom: 0, cursor: 'pointer' }}
          >
            Lai Food
          </Title>
        </Link>

        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
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
              <Button
                type="primary"
                shape="round"
                icon={<ShoppingCartOutlined />}
                onClick={() => navigate('/cart')}
              >
                购物车
              </Button>
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

      <Content style={{ padding: '32px 50px', maxWidth: 1200, margin: '0 auto', width: '100%' }}>
        {children}
      </Content>
    </Layout>
  );
};

export default LayoutComponent;
