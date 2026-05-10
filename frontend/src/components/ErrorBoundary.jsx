/**
 * ErrorBoundary 组件。
 *
 * React 错误边界：捕获子组件树中的 JavaScript 错误，
 * 记录错误信息，并显示降级 UI 而非崩溃的组件树。
 *
 * 使用方式：
 * <ErrorBoundary>
 *   <SomeComponentThatMightCrash />
 * </ErrorBoundary>
 *
 * 注意：React 错误边界必须是类组件。
 */

import { Component } from 'react';
import { Button, Result } from 'antd';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, info) {
    console.error('[ErrorBoundary] Caught error:', error, info);
  }

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
