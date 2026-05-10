/**
 * 餐厅列表与菜单页面。
 * 支持搜索餐厅、浏览菜单、添加购物车。
 */
import { useState } from 'react';
import {
  AutoComplete,
  Card,
  Col,
  Empty,
  Input,
  List,
  message,
  Row,
  Spin,
  Typography,
} from 'antd';
import { ShoppingCartOutlined, SearchOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { getRestaurants } from '../api/restaurantApi';
import { addItemToCart } from '../api/cartApi';
import { useMutation, useQueryClient } from '@tanstack/react-query';

const { Text, Title } = Typography;

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="150"%3E%3Crect fill="%23f0f0f0" width="200" height="150"/%3E%3Ctext x="50%25" y="50%25" font-family="sans-serif" font-size="14" fill="%23999" text-anchor="middle" dy=".3em"%3ENo Image%3C/text%3E%3C/svg%3E';

const RestaurantPage = () => {
  const [searchText, setSearchText] = useState('');
  const [expandedId, setExpandedId] = useState(null);

  const { data: restaurants, isLoading, error } = useQuery({
    queryKey: ['restaurants'],
    queryFn: getRestaurants,
    staleTime: 10 * 60 * 1000,
  });

  const queryClient = useQueryClient();
  const { mutate: addToCart } = useMutation({
    mutationFn: addItemToCart,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      message.success('已加入购物车');
    },
    onError: (err) => message.error(err.message),
  });

  if (isLoading) return <div style={{ textAlign: 'center', marginTop: 100 }}><Spin size="large" /></div>;
  if (error) return <Empty description={<Text type="danger">{error.message}</Text>} />;

  const filtered = (restaurants || []).filter(r =>
    r.name.toLowerCase().includes(searchText.toLowerCase())
  );
  const current = (restaurants || []).find(r => r.id === expandedId);

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <AutoComplete
          value={searchText}
          onSearch={setSearchText}
          onSelect={(v) => setExpandedId(Number(v))}
          onClear={() => { setSearchText(''); setExpandedId(null); }}
          options={filtered.map(r => ({ value: String(r.id), label: r.name }))}
          placeholder="搜索餐厅..."
          style={{ width: 300 }}
        >
          <Input suffix={<SearchOutlined />} allowClear />
        </AutoComplete>
      </div>

      <Row gutter={[16, 16]}>
        {filtered.map(r => (
          <Col key={r.id} xs={24} sm={12} md={8} lg={6}>
            <Card
              hoverable
              cover={
                <img alt={r.name} src={r.imageUrl}
                  style={{ height: 160, objectFit: 'cover' }}
                  onError={e => e.target.src = PLACEHOLDER_IMAGE}
                />
              }
              onClick={() => setExpandedId(expandedId === r.id ? null : r.id)}
              style={{ borderColor: expandedId === r.id ? '#1890ff' : undefined }}
            >
              <Card.Meta
                title={r.name}
                description={
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {r.address} · {r.menuItems?.length || 0} 道菜品
                  </Text>
                }
              />
            </Card>
          </Col>
        ))}
      </Row>

      {current && (
        <Card
          title={<Title level={4} style={{ marginBottom: 0 }}>{current.name}</Title>}
          extra={<Text type="secondary">共 {current.menuItems?.length || 0} 道菜品</Text>}
          style={{ marginTop: 24 }}
        >
          <List
            grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4 }}
            dataSource={current.menuItems || []}
            renderItem={item => (
              <List.Item>
                <Card
                  size="small"
                  cover={
                    <img src={item.imageUrl} alt={item.name}
                      style={{ height: 120, objectFit: 'cover' }}
                      onError={e => e.target.src = PLACEHOLDER_IMAGE}
                    />
                  }
                  actions={[
                    <ShoppingCartOutlined
                      key="add"
                      style={{ fontSize: 18, color: '#1890ff', cursor: 'pointer' }}
                      onClick={() => addToCart(item.id)}
                    />,
                  ]}
                >
                  <Card.Meta
                    title={item.name}
                    description={
                      <>
                        <Text ellipsis style={{ fontSize: 12 }}>{item.description || '暂无描述'}</Text>
                        <br />
                        <Text strong style={{ color: '#ff4d4f', fontSize: 15 }}>${item.price}</Text>
                      </>
                    }
                  />
                </Card>
              </List.Item>
            )}
          />
        </Card>
      )}
    </div>
  );
};

export default RestaurantPage;
