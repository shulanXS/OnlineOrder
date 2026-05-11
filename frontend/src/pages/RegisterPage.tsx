/**
 * 注册页面。
 *
 * 新用户注册账号。
 *
 * 表单设计：
 * - 邮箱：必填 + 格式校验
 * - 密码：必填 + 最少 8 位
 * - 确认密码：必填 + 与密码一致（antd 依赖校验）
 * - 名字/姓氏：必填
 *
 * 注册流程（React Query）：
 * 1. 用户填写表单 -> 点击注册
 * 2. 前端校验通过 -> 调用 register API
 * 3. 成功：setAuth() 自动保存 Token -> 提示"注册成功" -> 跳转餐厅页
 * 4. 失败：showError() 展示错误信息
 *
 * 设计要点：
 * - 注册成功后，后端直接返回 Token，无需用户二次登录
 * - setTimeout 延迟跳转，确保 message.success 能被用户看到
 */
import { Button, Card, Divider, Form, Input, message, Typography } from 'antd';
import { LockOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { useAuthStore } from '../stores/authStore';
import { register } from '../api/authApi';
import { useApiError } from '../hooks/useApiError';

const { Title } = Typography;

const RegisterPage = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const { showError } = useApiError();

  const { mutate, isPending } = useMutation({
    mutationFn: register,
    onSuccess: async (data) => {
      await setAuth(data.accessToken);
      message.success('注册成功');
      setTimeout(() => navigate('/restaurants'), 300);
    },
    onError: showError,
  });

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
      <Card style={{ width: 400 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3}>注册 Lai Food</Title>
        </div>
        <Form form={form} name="register" onFinish={mutate} layout="vertical" requiredMark={false}>
          <Form.Item
            name="email"
            rules={[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '邮箱格式不正确' },
            ]}
          >
            <Input prefix={<UserOutlined />} placeholder="邮箱" size="large" />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }, { min: 8, message: '至少8位' }]}
            extra="至少 8 位"
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            dependencies={['password']}
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次密码不一致'));
                },
              }),
            ]}
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
            <Button type="primary" htmlType="submit" loading={isPending} block size="large">
              注册
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default RegisterPage;
