/**
 * 注册页面。
 */
import { Button, Card, Divider, Form, Input, message, Typography } from 'antd';
import { LockOutlined, UserOutlined, TeamOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { useAuthStore } from '../stores/authStore';
import { register } from '../api/authApi';

const { Title } = Typography;

const RegisterPage = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);

  const { mutate, isPending } = useMutation({
    mutationFn: register,
    onSuccess: async (data) => {
      await setAuth(data.accessToken);
      message.success('注册成功');
      navigate('/restaurants');
    },
    onError: (err) => message.error(err.message),
  });

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
      <Card style={{ width: 400 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3}>注册 Lai Food</Title>
        </div>
        <Form form={form} name="register" onFinish={mutate} layout="vertical" requiredMark={false}>
          <Form.Item name="email" rules={[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '邮箱格式不正确' }]}>
            <Input prefix={<UserOutlined />} placeholder="邮箱" size="large" />
          </Form.Item>
          <Form.Item name="password"
            rules={[{ required: true, message: '请输入密码' }, { min: 8, message: '至少8位' }]}
            extra="至少 8 位"
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" />
          </Form.Item>
          <Form.Item name="confirmPassword" dependencies={['password']}
            rules={[{ required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, v) {
                  if (!v || getFieldValue('password') === v) return Promise.resolve();
                  return Promise.reject(new Error('两次密码不一致'));
                },
              })]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="确认密码" size="large" />
          </Form.Item>
          <Divider style={{ margin: '12px 0' }} />
          <Form.Item name="firstName" rules={[{ required: true, message: '请输入名字' }]}>
            <Input prefix={<TeamOutlined />} placeholder="名" size="large" />
          </Form.Item>
          <Form.Item name="lastName" rules={[{ required: true, message: '请输入姓氏' }]}>
            <Input prefix={<TeamOutlined />} placeholder="姓" size="large" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" loading={isPending} block size="large">注册</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default RegisterPage;
