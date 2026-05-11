/**
 * 餐厅列表与菜单页面。
 *
 * 支持搜索餐厅、浏览菜单、添加购物车。
 *
 * 搜索实现设计（受控搜索 vs 自动完成）：
 * - 使用受控的 Input + 内部 filtered 状态，而非依赖 AutoComplete 的非受控搜索
 * - onSearch 设为空函数，避免 AutoComplete 每次输入都触发过滤
 * - 输入时仅更新 searchText，由 useMemo 计算 filtered 结果
 * - 点击/回车选中时直接展开对应餐厅，而非依赖 AutoComplete 的过滤行为
 *
 * 餐厅展开策略：
 * - 点击餐厅卡片切换展开/折叠状态
 * - 从搜索建议项选中时自动展开对应餐厅
 * - 搜索词变化时自动折叠已展开的餐厅（避免显示不匹配的结果）
 * - 展开时使用 scrollIntoView 滚动到餐厅详情区域
 *
 * 性能优化：
 * - filtered 结果使用 useMemo 缓存，避免每次渲染都重新过滤
 * - current 餐厅使用 useMemo 缓存，避免在 JSX 中重复查找
 * - 餐厅数据缓存 10 分钟（RESTAURANTS_STALE_TIME），减少重复请求
 */
import { useState, useMemo, useRef, useEffect } from 'react';
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
import { CURRENCY_SYMBOL, RESTAURANTS_STALE_TIME, PLACEHOLDER_IMAGE } from '../constants';
import type { Restaurant } from '../types/api';

const { Text, Title } = Typography;

const RestaurantPage = () => {
  const [searchText, setSearchText] = useState('');
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const { showError } = useApiError();
  // ref 用于滚动到展开的餐厅详情区域
  const detailsRef = useRef<HTMLDivElement>(null);

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

  // 搜索过滤：按餐厅名称不区分大小写匹配
  const filtered = useMemo(() => {
    if (!restaurants) return [];
    if (!searchText.trim()) return restaurants;
    const lower = searchText.toLowerCase();
    return restaurants.filter((r) => r.name.toLowerCase().includes(lower));
  }, [restaurants, searchText]);

  // 当前展开的餐厅：从过滤结果中查找
  const current = useMemo(
    () => filtered.find((r) => r.id === expandedId) ?? null,
    [filtered, expandedId]
  );

  // 展开状态变化时，滚动到详情区域
  useEffect(() => {
    if (current && detailsRef.current) {
      setTimeout(() => {
        detailsRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 50);
    }
  }, [current]);

  // 搜索词变化时重置展开状态，避免显示不匹配的结果
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setSearchText(val);
    if (val !== '' && expandedId !== null) {
      setExpandedId(null);
    }
  };

  if (isLoading) {
    return <div style={{ textAlign: 'center', marginTop: 100 }}><Spin size="large" /></div>;
  }
  if (error) return <Empty description={<Text type="danger">{error.message}</Text>} />;

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <AutoComplete
          value={searchText}
          // onSearch 设为空：AutoComplete 的 onSearch 在输入时触发，
          // 会干扰受控的 searchText 状态。真正的过滤在 onChange 中通过 setSearchText 触发。
          onSearch={() => {}}
          // 点击建议项：展开对应餐厅并清空搜索词
          onSelect={(v) => {
            const id = Number(v);
            setExpandedId(id);
            setSearchText('');
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
            value={searchText}
            onChange={handleSearchChange}
            onPressEnter={() => {
              // 回车：自动展开搜索结果的第一个餐厅
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
                    onError={(e) => { (e.target as HTMLImageElement).src = PLACEHOLDER_IMAGE.restaurant; }}
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
        <div ref={detailsRef}>
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
                        onError={(e) => { (e.target as HTMLImageElement).src = PLACEHOLDER_IMAGE.restaurant; }}
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
        </div>
      )}
    </div>
  );
};

export default RestaurantPage;
