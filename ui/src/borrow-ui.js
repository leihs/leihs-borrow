import './scss/styles.scss'
import _IntlMessageFormat from 'intl-messageformat'
const OrderPanel = require('./features/OrderPanel')

export const Components = {
  // Feature components
  OrderPanel: OrderPanel.default,
  CategoryBreadcrumbs: require('./features/CategoryBreadcrumbs').default,
  ModelSearchFilter: require('./features/ModelSearchFilter').default,
  ModelList: require('./features/ModelList').default,
  ModelShow: require('./features/ModelShow').default,
  OrderSearchFilter: require('./features/OrderSearchFilter').default,
  UserProfilePage: require('./features/UserProfilePage').default,

  // Design components
  Design: {
    ActionButtonGroup: require('./components/ActionButtonGroup').default,
    Badge: require('./components/Badge').default,
    CircleBadge: require('./components/CircleBadge').default,
    ConfirmDialog: require('./components/ConfirmDialog').default,
    DatePicker: require('./components/DatePicker').default,
    DateRangePicker: require('./components/DateRangePicker').default,
    DownloadLink: require('./components/DownloadLink').default,
    ErrorNotification: require('./components/ErrorNotification').default,
    ErrorView: require('./components/ErrorView').default,
    FilterButton: require('./components/FilterButton').default,
    InfoMessage: require('./components/InfoMessage').default,
    InputWithClearButton: require('./components/InputWithClearButton').default,
    ListCard: require('./components/ListCard').default,
    ListMenu: require('./components/ListMenu').default,
    Menu: require('./components/Menu').default,
    MinusPlusControl: require('./components/MinusPlusControl').default,
    ModalDialog: require('./components/ModalDialog').default,
    PageLayout: require('./components/PageLayout').default,
    ProfileMenuButton: require('./components/ProfileMenuButton').default,
    ProgressInfo: require('./components/ProgressInfo').default,
    PropertyTable: require('./components/PropertyTable').default,
    Section: require('./components/Section').default,
    Spinner: require('./components/Spinner').default,
    SquareImage: require('./components/SquareImage').default,
    SquareImageGrid: require('./components/SquareImageGrid').default,
    TruncateText: require('./components/TruncateText').default,
    Icons: require('./components/Icons'),
    Textarea: require('./components/Textarea').default,
    Topnav: require('./components/Topnav').default,
    Warning: require('./components/Warning').default
  },

  // react-bootstrap
  ReactBootstrap: {
    Tab: require('react-bootstrap/Tab').default,
    Tabs: require('react-bootstrap/Tabs').default
  }
}

export const IntlMessageFormat = _IntlMessageFormat

export const validateDateRange = OrderPanel.validateDateRange
