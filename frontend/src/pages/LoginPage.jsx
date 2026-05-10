/**
 * 登录页面。
 */
import { Button, Card, Form, Input, message, Typography } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { useAuthStore } from '../stores/authStore';
import { login } from '../api/authApi';

const { Title } = Typography;

const LoginPage = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);

  const { mutate, isPending } = useMutation({
    mutationFn: login,
    onSuccess: async (data) => {
      await setAuth(data.accessToken);
      navigate('/restaurants');
    },
    onError: (err) => message.error(err.message),
  });

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
      <Card style={{ width: 400 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3}>登录 Lai Food</Title>
        </div>
        <Form form={form} name="login" onFinish={mutate} layout="vertical" requiredMark={false}>
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }, { type: 'email', message: '请输入有效邮箱' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名 / 邮箱" size="large" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" loading={isPending} block size="large">登录</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;
