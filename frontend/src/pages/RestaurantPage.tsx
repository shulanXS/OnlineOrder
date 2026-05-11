/**
 * 餐厅列表与菜单页面。
 * 支持搜索餐厅、浏览菜单、添加购物车。
 *
 * 搜索实现要点：
 * - 使用受控的 Input + 内部 filtered 状态，而非依赖 AutoComplete 的非受控搜索
 * - 输入时仅更新 filtered，不触发 API 请求
 * - 点击/回车选中时直接展开对应餐厅，而非依赖 AutoComplete 的过滤行为
 */
import { useState, useMemo } from 'react';
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
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getRestaurants } from '../api/restaurantApi';
import { addItemToCart } from '../api/cartApi';
import { useApiError } from '../hooks/useApiError';
import { CURRENCY_SYMBOL, RESTAURANTS_STALE_TIME } from '../constants';
import type { Restaurant } from '../types/api';

const { Text, Title } = Typography;

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="150"%3E%3Crect fill="%23f0f0f0" width="200" height="150"/%3E%3Ctext x="50%25" y="50%25" font-family="sans-serif" font-size="14" fill="%23999" text-anchor="middle" dy=".3em"%3ENo Image%3C/text%3E%3C/svg%3E';

const RestaurantPage = () => {
  const [searchText, setSearchText] = useState('');
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const { showError } = useApiError();

  const { data: restaurants, isLoading, error } = useQuery<Restaurant[]>({
    queryKey: ['restaurants'],
    queryFn: getRestaurants,
    staleTime: RESTAURANTS_STALE_TIME,
  });

  const queryClient = useQueryClient();
  const { mutate: addToCart } = useMutation({
    mutationFn: addItemToCart,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      message.success('已加入购物车');
    },
    onError: showError,
  });

  // 搜索过滤使用 useMemo 避免每次渲染都重新计算
  // 搜索条件：餐厅名称不区分大小写匹配
  const filtered = useMemo(() => {
    if (!restaurants) return [];
    if (!searchText.trim()) return restaurants;
    const lower = searchText.toLowerCase();
    return restaurants.filter((r) => r.name.toLowerCase().includes(lower));
  }, [restaurants, searchText]);

  // 当前展开的餐厅（从过滤结果中查找，避免搜索后找不到）
  const current = useMemo(
    () => filtered.find((r) => r.id === expandedId) ?? null,
    [filtered, expandedId]
  );

  if (isLoading) {
    return <div style={{ textAlign: 'center', marginTop: 100 }}><Spin size="large" /></div>;
  }
  if (error) return <Empty description={<Text type="danger">{error.message}</Text>} />;

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <AutoComplete
          value={searchText}
          // onSearch 只在用户选择建议项时触发（不触发过滤）
          // 输入时仅更新 searchText，由 filtered useMemo 计算结果
          onSearch={() => {}}
          // 点击建议项：展开对应餐厅
          onSelect={(v) => {
            const id = Number(v);
            setExpandedId(id);
            // 清空搜索并定位到该餐厅（通过滚动）
          }}
          onClear={() => {
            setSearchText('');
            setExpandedId(null);
          }}
          options={filtered.map((r) => ({ value: String(r.id), label: r.name }))}
          placeholder="搜索餐厅..."
          style={{ width: 300 }}
        >
          <Input
            suffix={<SearchOutlined />}
            allowClear
            // 独立的 onChange 处理输入，与 AutoComplete 解耦
            onChange={(e) => setSearchText(e.target.value)}
            // 回车：选中搜索结果的第一个
            onPressEnter={() => {
              if (filtered.length > 0 && !expandedId) {
                setExpandedId(filtered[0].id);
              }
            }}
          />
        </AutoComplete>
      </div>

      {filtered.length === 0 ? (
        <Empty description="没有找到匹配的餐厅" style={{ marginTop: 60 }} />
      ) : (
        <Row gutter={[16, 16]}>
          {filtered.map((r) => (
            <Col key={r.id} xs={24} sm={12} md={8} lg={6}>
              <Card
                hoverable
                cover={
                  <img
                    alt={r.name}
                    src={r.imageUrl}
                    loading="lazy"
                    style={{ height: 160, objectFit: 'cover' }}
                    onError={(e) => { (e.target as HTMLImageElement).src = PLACEHOLDER_IMAGE; }}
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
      )}

      {current && (
        <Card
          title={<Title level={4} style={{ marginBottom: 0 }}>{current.name}</Title>}
          extra={<Text type="secondary">共 {current.menuItems?.length || 0} 道菜品</Text>}
          style={{ marginTop: 24 }}
        >
          <List
            grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4 }}
            dataSource={current.menuItems || []}
            renderItem={(item) => (
              <List.Item>
                <Card
                  size="small"
                  cover={
                    <img
                      src={item.imageUrl}
                      alt={item.name}
                      loading="lazy"
                      style={{ height: 120, objectFit: 'cover' }}
                      onError={(e) => { (e.target as HTMLImageElement).src = PLACEHOLDER_IMAGE; }}
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
                        <Text ellipsis style={{ fontSize: 12 }}>
                          {item.description || '暂无描述'}
                        </Text>
                        <br />
                        <Text strong style={{ color: '#ff4d4f', fontSize: 15 }}>
                          {CURRENCY_SYMBOL}{item.price}
                        </Text>
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
