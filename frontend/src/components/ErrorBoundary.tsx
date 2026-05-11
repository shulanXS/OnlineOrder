/**
 * ErrorBoundary 组件。
 *
 * React 错误边界：捕获子组件树中的 JavaScript 错误，
 * 记录错误信息，并显示降级 UI 而非崩溃的组件树。
 *
 * React 错误边界规则：
 * - 必须是类组件（ErrorBoundary 使用此模式）
 * - 只能捕获"渲染阶段"的错误，事件处理、异步代码的错误不受影响
 * - getDerivedStateFromError：将错误转换为状态，触发降级 UI 渲染
 * - componentDidCatch：记录错误信息（用于上报监控）
 *
 * 降级 UI：
 * - 显示友好的错误提示（不暴露技术细节）
 * - 提供"刷新页面"和"重试"两个操作按钮
 *
 * 使用方式：
 *   <ErrorBoundary>
 *     <SomeComponentThatMightCrash />
 *   </ErrorBoundary>
 */
import { Component, ReactNode } from 'react';
import { Button, Result } from 'antd';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

/**
 * React 错误边界组件。
 *
 * 使用泛型约束 Props 和 State 类型，
 * 确保 TypeScript 能正确推断状态和属性。
 */
class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  /**
   * 静态方法：将错误转换为状态。
   * 在渲染阶段调用，返回新的 state 触发降级 UI。
   */
  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  /**
   * 生命周期方法：记录错误到控制台。
   * 生产环境应替换为监控服务上报（如 Sentry）。
   */
  componentDidCatch(error: Error, info: React.ErrorInfo) {
    console.error('[ErrorBoundary] Caught error:', error, info);
  }

  /** 重置错误状态，尝试恢复页面 */
  handleReset = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      return (
        <Result
          status="error"
          title="页面渲染出错"
          subTitle="抱歉，应用遇到了一个错误。请尝试刷新页面。"
          extra={
            <>
              <Button type="primary" onClick={() => window.location.reload()}>
                刷新页面
              </Button>
              <Button onClick={this.handleReset}>重试</Button>
            </>
          }
        />
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
