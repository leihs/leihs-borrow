import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import { ErrorBoundary } from './ErrorBoundary'

export default function PageLayout({
  children,
  topBar,
  navOverlay,
  navOverlayShown,
  onContentClick,
  className,
  errorBoundaryTxt,
  ...restProps
}) {
  errorBoundaryTxt = {
    title: 'Error displaying this content',
    reload: 'Reload current page',
    goToStart: 'Go to start page',
    ...errorBoundaryTxt
  }
  return (
    <div className={cx('ui-page-layout', 'page-layout', className)} {...restProps}>
      <div className="page-layout__top-row">
        <div className="page-layout__top-nav-container">
          <ErrorBoundary txt={errorBoundaryTxt}>{topBar}</ErrorBoundary>
        </div>
      </div>
      <div className="page-layout__main-row">
        {navOverlayShown && navOverlay && (
          <div className="page-layout__nav-overlay">
            <div className="page-layout__nav-container">
              <ErrorBoundary txt={errorBoundaryTxt}>{navOverlay}</ErrorBoundary>
            </div>
          </div>
        )}

        <div className="ui-page-content page-layout__content" onClick={onContentClick}>
          <ErrorBoundary txt={errorBoundaryTxt}>{children}</ErrorBoundary>
        </div>
      </div>
    </div>
  )
}

PageLayout.Header = function PageLayoutHeader({ preTitle, title, subTitle, children, className, ...restProps }) {
  return (
    <div className={cx('text-center-if-layout-allows mb-4', className)} {...restProps}>
      <h1>
        {preTitle && (
          <>
            <span className="d-inline-block fs-2 mb-1">{preTitle}</span>
            <br />
          </>
        )}
        {title}
      </h1>
      {subTitle && <h2>{subTitle}</h2>}
      {children}
    </div>
  )
}
PageLayout.Header.displayName = 'PageLayout.Header'

PageLayout.ContentContainer = function PageLayoutContentContainer({ children, className, ...restProps }) {
  return (
    <div className={cx('page-layout__content-container page-inset-x', className)} {...restProps}>
      {children}
    </div>
  )
}

PageLayout.propTypes = {
  /** Element to show in content zone */
  children: PropTypes.node,
  /** Element to show in the top bar zone */
  topBar: PropTypes.node,
  /** Nav overlay (for mobile / small screens) */
  navOverlay: PropTypes.node,
  /** Nav overlay shown? */
  navOverlayShown: PropTypes.bool,
  /** Emitted on click in content zone (so the controlling component can hide the overlay)  */
  onContentClick: PropTypes.func,
  /** CSS class of the wrapping element */
  className: PropTypes.string,
  /** Localized texts for the error screen */
  errorBoundaryTxt: PropTypes.shape({
    title: PropTypes.string,
    reload: PropTypes.string,
    goToStart: PropTypes.string
  })
}
