/**
 * 登录页面。
 *
 * 用户输入邮箱和密码进行身份认证。
 *
 * 表单设计：
 * - 邮箱字段：必填 + 格式校验（antd 内置 email 类型）
 * - 密码字段：必填
 * - 表单验证失败时阻止提交，不发请求
 *
 * 认证流程（React Query）：
 * 1. 用户点击登录 -> onFinish 触发 mutation
 * 2. mutationFn 调用 login API
 * 3. 成功：setAuth() 保存 Token + 获取用户信息 -> 提示"登录成功" -> 跳转餐厅页
 * 4. 失败：showError() 展示错误信息（由 apiClient 拦截器注入 message）
 */
import { Button, Card, Form, Input, message, Typography } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { useAuthStore } from '../stores/authStore';
import { login } from '../api/authApi';
import { useApiError } from '../hooks/useApiError';

const { Title } = Typography;

const LoginPage = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const { showError } = useApiError();

  const { mutate, isPending } = useMutation({
    mutationFn: login,
    onSuccess: async (data) => {
      await setAuth(data.accessToken);
      message.success('登录成功');
      navigate('/restaurants');
    },
    onError: showError,
  });

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
      <Card style={{ width: 400 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3}>登录 Lai Food</Title>
        </div>
        <Form form={form} name="login" onFinish={mutate} layout="vertical" requiredMark={false}>
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名' },
              { type: 'email', message: '请输入有效邮箱' },
            ]}
          >
            <Input prefix={<UserOutlined />} placeholder="用户名 / 邮箱" size="large" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" loading={isPending} block size="large">
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;
